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
    
    // Store pending audits for door open/close events
    private final Map<String, Long> pendingAudits = new ConcurrentHashMap<>();
    
    @PostMapping("/database-command")
    @Operation(summary = "Process database command", description = "Accepts database commands (INSERT, UPDATE, DELETE, SYNC) and applies them", security = @SecurityRequirement(name = "basicAuth"))
    public ResponseEntity<CommandAcknowledgement> processDatabaseCommand(
            @RequestBody DatabaseCommand command) {
        
        CommandAcknowledgement ack = databaseCommandService.processCommand(command);
        return ResponseEntity.ok(ack);
    }
    
    @PostMapping("/command-ack")
    @Operation(summary = "Receive command acknowledgement", description = "Accepts command acknowledgements from controllers", security = @SecurityRequirement(name = "basicAuth"))
    public ResponseEntity<Map<String, String>> receiveCommandAck(
            @RequestBody CommandAcknowledgement ack) {
        
        Map<String, String> response = new HashMap<>();
        response.put("status", "received");
        response.put("command_id", ack.getCommandId());
        response.put("timestamp", String.valueOf(System.currentTimeMillis()));
        
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/event-log")
    @Operation(summary = "Process event log", description = "Processes card scans and system events for real-time access validation", security = @SecurityRequirement(name = "basicAuth"))
    public ResponseEntity<EventLogResponse> processEventLog(@RequestBody EventLog eventLog) {
        
        EventLogResponse response = new EventLogResponse();
        response.setEventId(eventLog.getEventId());
        response.setTimestamp(System.currentTimeMillis());
        
        LocalDateTime eventTime = LocalDateTime.ofInstant(
            Instant.ofEpochMilli(eventLog.getTimestamp()), ZoneOffset.UTC);
        
        if ("card_scan".equals(eventLog.getEventType())) {
            // Validate card access
            CardValidationService.CardValidationResult validation = 
                cardValidationService.validateCardAccess(eventLog.getCardHex(), eventLog.getDoorId());
            
            // Find reader for this door
            Long readerId = readerRepository.findByDoorIdAndIsActiveTrue(eventLog.getDoorId())
                .map(r -> r.getReaderId())
                .orElse(null);
            
            if (validation.isGranted()) {
                // Create audit entry for card scan
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
                
                // Store audit ID for subsequent door open/close events
                String key = eventLog.getCardHex() + "_" + eventLog.getDoorId() + "_" + eventLog.getTimestamp();
                pendingAudits.put(key, audit.getAuditId());
                
                response.setEventType("access_granted");
                response.setDoorType(validation.getDoorType());
                response.setMessage("Access granted");
                
            } else {
                // Create audit entry for denied access
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
            
        } else if ("system_event".equals(eventLog.getEventType())) {
            // Handle door open/close events
            String details = eventLog.getDetails() != null ? eventLog.getDetails().toLowerCase() : "";
            
            // Find the most recent audit for this card/door combination
            Audit latestAudit = null;
            if (eventLog.getCardHex() != null && eventLog.getDoorId() != null) {
                // Try to find by card
                var cardOpt = cardValidationService.findCardByUid(eventLog.getCardHex());
                if (cardOpt.isPresent()) {
                    latestAudit = auditService.findLatestAuditByCardAndDoor(
                        cardOpt.get().getCardId(), eventLog.getDoorId());
                }
            }
            
            if (latestAudit != null) {
                if (details.contains("open") || details.contains("opened")) {
                    auditService.updateAuditWithDoorOpen(latestAudit.getAuditId(), eventTime);
                } else if (details.contains("close") || details.contains("closed")) {
                    auditService.updateAuditWithDoorClose(latestAudit.getAuditId(), eventTime);
                }
            }
            
            response.setEventType("system_event");
            response.setMessage("System event processed");
            
        } else if ("access_granted".equals(eventLog.getEventType()) || 
                   "access_denied".equals(eventLog.getEventType())) {
            // Acknowledge access responses
            response.setEventType(eventLog.getEventType());
            response.setMessage("Event acknowledged");
        }
        
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/server-heartbeat")
    @Operation(summary = "Receive server heartbeat", description = "Accepts heartbeat messages from controllers", security = @SecurityRequirement(name = "basicAuth"))
    public ResponseEntity<ServerHeartbeatResponse> receiveHeartbeat(
            @RequestBody ServerHeartbeat heartbeat) {
        
        ServerHeartbeatResponse response = new ServerHeartbeatResponse();
        response.setDeviceId(heartbeat.getDeviceId());
        response.setTimestampReceived(System.currentTimeMillis());
        response.setServerStatus("online");
        response.setServerTimestamp(System.currentTimeMillis());
        
        return ResponseEntity.ok(response);
    }
}
