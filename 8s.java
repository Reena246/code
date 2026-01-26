package com.accesscontrol.controller;

import com.accesscontrol.dto.*;
import com.accesscontrol.exception.EncryptionException;
import com.accesscontrol.security.AesEncryptionUtil;
import com.accesscontrol.service.BulkEventService;
import com.accesscontrol.service.DbSyncService;
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

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Controller for database sync, bulk events, and health checks
 * All payloads are encrypted with AES-256/CBC/PKCS5Padding
 */
@RestController
@RequestMapping("/controller")
@Tag(name = "Controller Sync", description = "Database synchronization and offline event handling")
public class ControllerSyncController {

    private static final Logger logger = LoggerFactory.getLogger(ControllerSyncController.class);
    private static final String IV_HEADER = "X-IV";

    private final DbSyncService dbSyncService;
    private final BulkEventService bulkEventService;
    private final AesEncryptionUtil encryptionUtil;

    public ControllerSyncController(DbSyncService dbSyncService,
                                   BulkEventService bulkEventService,
                                   AesEncryptionUtil encryptionUtil) {
        this.dbSyncService = dbSyncService;
        this.bulkEventService = bulkEventService;
        this.encryptionUtil = encryptionUtil;
    }

    @PostMapping(value = "/db-sync",
                 consumes = MediaType.APPLICATION_OCTET_STREAM_VALUE,
                 produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    @Operation(
        summary = "Database synchronization",
        description = "Sync allowed cards for each reader. Returns minimal payload optimized for offline controllers. " +
                     "Triggered periodically or on reconnect.",
        parameters = {
            @Parameter(name = "X-IV", description = "Base64-encoded IV for encryption/decryption", required = true)
        },
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Encrypted DbSyncRequest: {controllerMac}",
            content = @Content(mediaType = "application/octet-stream")
        ),
        responses = {
            @ApiResponse(responseCode = "200", description = "Sync data returned",
                content = @Content(mediaType = "application/octet-stream",
                    schema = @Schema(description = "Encrypted DbSyncResponse: {readers: [{readerUuid, allowedCards: [cardUid]}]}"))),
            @ApiResponse(responseCode = "400", description = "Invalid request or encryption error")
        }
    )
    public ResponseEntity<byte[]> dbSync(
            @RequestHeader(IV_HEADER) String iv,
            @RequestBody byte[] encryptedRequest) {

        try {
            // Validate IV
            if (!encryptionUtil.isValidIV(iv)) {
                throw new EncryptionException("Invalid or missing X-IV header");
            }

            // Decrypt request
            DbSyncRequest request = encryptionUtil.decrypt(
                    encryptedRequest, iv, DbSyncRequest.class);

            logger.info("DB sync request - controller_mac: {}", request.getControllerMac());

            // Process sync
            DbSyncResponse response = dbSyncService.syncDatabase(request);

            // Encrypt response with SAME IV
            byte[] encryptedResponse = encryptionUtil.encrypt(response, iv);

            logger.info("DB sync completed - controller_mac: {}, readers: {}, total_cards: {}",
                    request.getControllerMac(),
                    response.getReaders().size(),
                    response.getReaders().stream()
                            .mapToInt(r -> r.getAllowedCards().size()).sum());

            return ResponseEntity.ok()
                    .header(IV_HEADER, iv)
                    .body(encryptedResponse);

        } catch (EncryptionException e) {
            logger.error("Encryption error: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("DB sync error: {}", e.getMessage(), e);
            throw new RuntimeException("DB sync failed: " + e.getMessage(), e);
        }
    }

    @PostMapping(value = "/bulk-event-logs",
                 consumes = MediaType.APPLICATION_OCTET_STREAM_VALUE,
                 produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    @Operation(
        summary = "Upload bulk event logs",
        description = "Upload events collected during offline mode. Events are processed chronologically. " +
                     "Called after controller reconnects.",
        parameters = {
            @Parameter(name = "X-IV", description = "Base64-encoded IV for encryption/decryption", required = true)
        },
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Encrypted BulkEventLogsRequest: {controllerMac, events: [{readerUuid, cardUid, eventType, eventTime}]}",
            content = @Content(mediaType = "application/octet-stream")
        ),
        responses = {
            @ApiResponse(responseCode = "200", description = "Events received and processed",
                content = @Content(mediaType = "application/octet-stream",
                    schema = @Schema(description = "Encrypted BulkEventLogsResponse: {status, processedCount}"))),
            @ApiResponse(responseCode = "400", description = "Invalid request or encryption error")
        }
    )
    public ResponseEntity<byte[]> bulkEventLogs(
            @RequestHeader(IV_HEADER) String iv,
            @RequestBody byte[] encryptedRequest) {

        try {
            // Validate IV
            if (!encryptionUtil.isValidIV(iv)) {
                throw new EncryptionException("Invalid or missing X-IV header");
            }

            // Decrypt request
            BulkEventLogsRequest request = encryptionUtil.decrypt(
                    encryptedRequest, iv, BulkEventLogsRequest.class);

            logger.info("Bulk event logs request - controller_mac: {}, event_count: {}",
                    request.getControllerMac(),
                    request.getEvents() != null ? request.getEvents().size() : 0);

            // Process bulk events
            BulkEventLogsResponse response = bulkEventService.processBulkEvents(request);

            // Encrypt response with SAME IV
            byte[] encryptedResponse = encryptionUtil.encrypt(response, iv);

            return ResponseEntity.ok()
                    .header(IV_HEADER, iv)
                    .body(encryptedResponse);

        } catch (EncryptionException e) {
            logger.error("Encryption error: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Bulk event logs error: {}", e.getMessage(), e);
            throw new RuntimeException("Bulk event logs processing failed: " + e.getMessage(), e);
        }
    }

    @PostMapping(value = "/ping",
                 consumes = MediaType.APPLICATION_OCTET_STREAM_VALUE,
                 produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    @Operation(
        summary = "Controller health check",
        description = "Verify connectivity and get server time.",
        parameters = {
            @Parameter(name = "X-IV", description = "Base64-encoded IV for encryption/decryption", required = true)
        },
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Encrypted PingRequest: {controllerMac}",
            content = @Content(mediaType = "application/octet-stream")
        ),
        responses = {
            @ApiResponse(responseCode = "200", description = "Ping successful",
                content = @Content(mediaType = "application/octet-stream",
                    schema = @Schema(description = "Encrypted PingResponse: {status, serverTime}"))),
            @ApiResponse(responseCode = "400", description = "Invalid request or encryption error")
        }
    )
    public ResponseEntity<byte[]> ping(
            @RequestHeader(IV_HEADER) String iv,
            @RequestBody byte[] encryptedRequest) {

        try {
            // Validate IV
            if (!encryptionUtil.isValidIV(iv)) {
                throw new EncryptionException("Invalid or missing X-IV header");
            }

            // Decrypt request
            PingRequest request = encryptionUtil.decrypt(
                    encryptedRequest, iv, PingRequest.class);

            logger.debug("Ping request - controller_mac: {}", request.getControllerMac());

            // Create response
            PingResponse response = new PingResponse(
                    "OK",
                    LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME)
            );

            // Encrypt response with SAME IV
            byte[] encryptedResponse = encryptionUtil.encrypt(response, iv);

            return ResponseEntity.ok()
                    .header(IV_HEADER, iv)
                    .body(encryptedResponse);

        } catch (EncryptionException e) {
            logger.error("Encryption error: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Ping error: {}", e.getMessage(), e);
            throw new RuntimeException("Ping failed: " + e.getMessage(), e);
        }
    }
}
