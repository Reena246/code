package com.project.accesscontrol.controller;

import com.project.accesscontrol.dto.*;
import com.project.accesscontrol.entity.Audit;
import com.project.accesscontrol.repository.ReaderRepository;
import com.project.accesscontrol.service.AuditService;
import com.project.accesscontrol.service.CardValidationService;
import com.project.accesscontrol.service.DatabaseCommandService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api")
@Tag(name = "Access Control API", description = "API endpoints for access control system")
public class ApiController {

    @Autowired
    private DatabaseCommandService databaseCommandService;

    @Autowired
    private CardValidationService cardValidationService;

    @Autowired
    private AuditService auditService;

    @Autowired
    private ReaderRepository readerRepository;

    // Map to track pending audits for door open/close events
    private final Map<String, Long> pendingAudits = new ConcurrentHashMap<>();

    @PostMapping("/database-command")
    @Operation(summary = "Process database command", security = @SecurityRequirement(name = "basicAuth"))
    public ResponseEntity<CommandAcknowledgement> processDatabaseCommand(
            @RequestBody DatabaseCommand command) {
        CommandAcknowledgement ack = databaseCommandService.processCommand(command);
        return ResponseEntity.ok(ack);
    }

    @PostMapping("/command-ack")
    @Operation(summary = "Receive command acknowledgement", security = @SecurityRequirement(name = "basicAuth"))
    public ResponseEntity<Map<String, String>> receiveCommandAck(
            @RequestBody CommandAcknowledgement ack) {
        Map<String, String> response = new HashMap<>();
        response.put("status", "received");
        response.put("command_id", ack.getCommandId());
        response.put("timestamp", String.valueOf(System.currentTimeMillis()));
        return ResponseEntity.ok(response);
    }

    @PostMapping("/event-log")
    @Operation(summary = "Process event log", security = @SecurityRequirement(name = "basicAuth"))
    public ResponseEntity<EventLogResponse> processEventLog(@RequestBody EventLog eventLog) {

        EventLogResponse response = new EventLogResponse();
        response.setEventId(eventLog.getEventId());
        response.setTimestamp(System.currentTimeMillis());

        // Use controller timestamp if present, else fallback to now
        LocalDateTime eventTime = eventLog.getTimestamp() != null
                ? LocalDateTime.ofInstant(Instant.ofEpochMilli(eventLog.getTimestamp()), ZoneOffset.UTC)
                : LocalDateTime.now();

        String key = eventLog.getCardHex() + "_" + eventLog.getDoorId(); // Track audits by card+door

        switch (eventLog.getEventType()) {
            case "card_scan":
                // Validate card access
                CardValidationService.CardValidationResult validation =
                        cardValidationService.validateCardAccess(eventLog.getCardHex(), eventLog.getDoorId());

                Long readerId = readerRepository.findByDoorIdAndIsActiveTrue(eventLog.getDoorId())
                        .map(r -> r.getReaderId()).orElse(null);

                if (validation.isGranted()) {
                    Audit audit = auditService.createCardScanAudit(
                            validation.getCompanyId(),
                            validation.getEmployeePk(),
                            validation.getCardId(),
                            eventLog.getDoorId(),
                            readerId,
                            eventTime,
                            true,
                            validation.getReason()
                    );

                    // Store audit ID for subsequent open/close events
                    pendingAudits.put(key, audit.getAuditId());

                    response.setEventType("access_granted");
                    response.setDoorType(validation.getDoorType());
                    response.setMessage("Access granted");
                } else {
                    auditService.createCardScanAudit(
                            eventLog.getCompanyId(),
                            null,
                            null,
                            eventLog.getDoorId(),
                            readerId,
                            eventTime,
                            false,
                            validation.getReason()
                    );

                    response.setEventType("access_denied");
                    response.setDoorType(null);
                    response.setMessage(validation.getReason());
                }
                break;

            case "system_event":
                Long auditId = pendingAudits.get(key);
                if (auditId != null) {
                    String details = eventLog.getDetails() != null ? eventLog.getDetails().toLowerCase() : "";
                    if (details.contains("open") || details.contains("opened")) {
                        auditService.updateAuditWithDoorOpen(auditId, eventTime);
                        response.setMessage("Door open logged");
                    } else if (details.contains("close") || details.contains("closed")) {
                        auditService.updateAuditWithDoorClose(auditId, eventTime);
                        response.setMessage("Door close logged");
                        // Remove from pending audits after door is closed
                        pendingAudits.remove(key);
                    }
                    response.setEventType("system_event");
                } else {
                    response.setEventType("system_event");
                    response.setMessage("No pending audit found for this card and door");
                }
                break;

            case "access_granted":
            case "access_denied":
                // Controller acknowledging access response
                response.setEventType(eventLog.getEventType());
                response.setMessage("Event acknowledged");
                break;

            default:
                response.setEventType("unknown");
                response.setMessage("Unknown event type");
        }

        return ResponseEntity.ok(response);
    }

    @PostMapping("/server-heartbeat")
    @Operation(summary = "Receive server heartbeat", security = @SecurityRequirement(name = "basicAuth"))
    public ResponseEntity<ServerHeartbeatResponse> receiveHeartbeat(@RequestBody ServerHeartbeat heartbeat) {
        ServerHeartbeatResponse response = new ServerHeartbeatResponse();
        response.setDeviceId(heartbeat.getDeviceId());
        response.setTimestampReceived(System.currentTimeMillis());
        response.setServerStatus("online");
        response.setServerTimestamp(System.currentTimeMillis());
        return ResponseEntity.ok(response);
    }
}
