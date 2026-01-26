package com.accesscontrol.controller;

import com.accesscontrol.dto.ValidateAccessRequest;
import com.accesscontrol.dto.ValidateAccessResponse;
import com.accesscontrol.exception.EncryptionException;
import com.accesscontrol.security.AesEncryptionUtil;
import com.accesscontrol.service.AccessControlService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * Controller for real-time access validation
 * All payloads are encrypted with AES-256/CBC/PKCS5Padding
 */
@RestController
@RequestMapping("/access")
@Tag(name = "Access Control", description = "Real-time card validation and access control")
public class AccessControlController {

    private static final Logger logger = LoggerFactory.getLogger(AccessControlController.class);
    private static final String IV_HEADER = "X-IV";

    private final AccessControlService accessControlService;
    private final AesEncryptionUtil encryptionUtil;

    public AccessControlController(AccessControlService accessControlService,
                                   AesEncryptionUtil encryptionUtil) {
        this.accessControlService = accessControlService;
        this.encryptionUtil = encryptionUtil;
    }

    @PostMapping(value = "/validate", 
                 consumes = MediaType.APPLICATION_OCTET_STREAM_VALUE,
                 produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    @Operation(
        summary = "Validate access card",
        description = "Real-time validation of access card. Request and response are encrypted with AES-256/CBC. " +
                     "IV must be provided via X-IV header (Base64 encoded). Same IV is used for response encryption.",
        parameters = {
            @Parameter(name = "X-IV", description = "Base64-encoded IV for encryption/decryption", required = true)
        },
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Encrypted ValidateAccessRequest: {controllerMac, readerUuid, cardHex, timestamp}",
            content = @Content(mediaType = "application/octet-stream")
        ),
        responses = {
            @ApiResponse(responseCode = "200", description = "Access validated",
                content = @Content(mediaType = "application/octet-stream",
                    schema = @Schema(description = "Encrypted ValidateAccessResponse: {result, lockType, readerUuid, reason}"))),
            @ApiResponse(responseCode = "400", description = "Invalid request or encryption error"),
            @ApiResponse(responseCode = "404", description = "Controller not found")
        }
    )
    public ResponseEntity<byte[]> validateAccess(
            @RequestHeader(IV_HEADER) String iv,
            @RequestBody byte[] encryptedRequest) {

        LocalDateTime requestReceivedAt = LocalDateTime.now();

        try {
            // Validate IV
            if (!encryptionUtil.isValidIV(iv)) {
                throw new EncryptionException("Invalid or missing X-IV header");
            }

            // Decrypt request
            ValidateAccessRequest request = encryptionUtil.decrypt(
                    encryptedRequest, iv, ValidateAccessRequest.class);

            logger.info("Access validation request - controller_mac: {}, reader_uuid: {}",
                    request.getControllerMac(), request.getReaderUuid());

            // Process validation
            ValidateAccessResponse response = accessControlService.validateAccess(
                    request, requestReceivedAt);

            // Encrypt response with SAME IV
            byte[] encryptedResponse = encryptionUtil.encrypt(response, iv);

            return ResponseEntity.ok()
                    .header(IV_HEADER, iv)
                    .body(encryptedResponse);

        } catch (EncryptionException e) {
            logger.error("Encryption error: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Validation error: {}", e.getMessage(), e);
            throw new RuntimeException("Validation failed: " + e.getMessage(), e);
        }
    }
}
