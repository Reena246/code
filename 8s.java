package com.project.badgemate.controller;

import com.project.badgemate.dto.*;
import com.project.badgemate.service.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@Tag(name = "Badgemate API", description = "Access control system APIs")
public class ApiController {
    
    @Autowired
    private DatabaseCommandService databaseCommandService;
    
    @Autowired
    private CommandAckService commandAckService;
    
    @Autowired
    private EventLogService eventLogService;
    
    @Autowired
    private ServerHeartbeatService serverHeartbeatService;
    
    @PostMapping("/database-command")
    @Operation(summary = "Process database command", 
               description = "Accepts database commands (INSERT, UPDATE, DELETE, SYNC, SYNC_RESPONSE) and returns acknowledgment")
    public ResponseEntity<CommandAcknowledgement> processDatabaseCommand(
            @Valid @RequestBody DatabaseCommand command) {
        CommandAcknowledgement ack = databaseCommandService.processDatabaseCommand(command);
        return ResponseEntity.ok(ack);
    }
    
    @PostMapping("/command-ack")
    @Operation(summary = "Receive command acknowledgment", 
               description = "Accepts command acknowledgments from external systems")
    public ResponseEntity<String> receiveCommandAck(
            @Valid @RequestBody CommandAcknowledgement ack) {
        commandAckService.processCommandAcknowledgement(ack);
        return ResponseEntity.ok("Acknowledgment received");
    }
    
    @PostMapping("/event-log")
    @Operation(summary = "Process event log", 
               description = "Processes card scan events, validates access, and creates audit entries")
    public ResponseEntity<EventLogResponse> processEventLog(
            @Valid @RequestBody EventLog eventLog) {
        EventLogResponse response = eventLogService.processEventLog(eventLog);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/server-heartbeat")
    @Operation(summary = "Receive server heartbeat", 
               description = "Accepts heartbeat messages from devices/servers")
    public ResponseEntity<ServerHeartbeatResponse> receiveHeartbeat(
            @Valid @RequestBody ServerHeartbeat heartbeat) {
        ServerHeartbeatResponse response = serverHeartbeatService.processHeartbeat(heartbeat);
        return ResponseEntity.ok(response);
    }
}
