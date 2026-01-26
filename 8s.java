package com.accesscontrol.controller;

import com.accesscontrol.dto.DoorEventRequest;
import com.accesscontrol.dto.DoorEventResponse;
import com.accesscontrol.exception.EncryptionException;
import com.accesscontrol.security.AesEncryptionUtil;
import com.accesscontrol.service.DoorEventService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for door events (OPEN, CLOSE, FORCED)
 * All payloads are encrypted with AES-256/CBC/PKCS5Padding
 */
@RestController
@RequestMapping("/access")
@Tag(name = "Door Events", description = "Handle door state change events")
public class DoorEventController {

    private static final Logger logger = LoggerFactory.getLogger(DoorEventController.class);
    private static final String IV_HEADER = "X-IV";

    private final DoorEventService doorEventService;
    private final AesEncryptionUtil encryptionUtil;

    public DoorEventController(DoorEventService doorEventService,
                              AesEncryptionUtil encryptionUtil) {
        this.doorEventService = doorEventService;
        this.encryptionUtil = encryptionUtil;
    }

    @PostMapping(value = "/event",
                 consumes = MediaType.APPLICATION_OCTET_STREAM_VALUE,
                 produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    @Operation(
        summary = "Log door event",
        description = "Record door state changes (OPEN/CLOSE/FORCED). Request and response are encrypted with AES-256/CBC.",
        parameters = {
            @Parameter(name = "X-IV", description = "Base64-encoded IV for encryption/decryption", required = true)
        },
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Encrypted DoorEventRequest: {controllerMac, readerUuid, eventType, timestamp}",
            content = @Content(mediaType = "application/octet-stream")
        ),
        responses = {
            @ApiResponse(responseCode = "200", description = "Event recorded",
                content = @Content(mediaType = "application/octet-stream",
                    schema = @Schema(description = "Encrypted DoorEventResponse: {status}"))),
            @ApiResponse(responseCode = "400", description = "Invalid request or encryption error")
        }
    )
    public ResponseEntity<byte[]> logDoorEvent(
            @RequestHeader(IV_HEADER) String iv,
            @RequestBody byte[] encryptedRequest) {

        try {
            // Validate IV
            if (!encryptionUtil.isValidIV(iv)) {
                throw new EncryptionException("Invalid or missing X-IV header");
            }

            // Decrypt request
            DoorEventRequest request = encryptionUtil.decrypt(
                    encryptedRequest, iv, DoorEventRequest.class);

            logger.info("Door event request - controller_mac: {}, reader_uuid: {}, event_type: {}",
                    request.getControllerMac(), request.getReaderUuid(), request.getEventType());

            // Process event
            doorEventService.processDoorEvent(request);

            // Create response
            DoorEventResponse response = new DoorEventResponse("OK");

            // Encrypt response with SAME IV
            byte[] encryptedResponse = encryptionUtil.encrypt(response, iv);

            return ResponseEntity.ok()
                    .header(IV_HEADER, iv)
                    .body(encryptedResponse);

        } catch (EncryptionException e) {
            logger.error("Encryption error: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Door event error: {}", e.getMessage(), e);
            throw new RuntimeException("Door event processing failed: " + e.getMessage(), e);
        }
    }
}
