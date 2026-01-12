package com.company.badgemate.controller;

import com.company.badgemate.dto.ServerHeartbeat;
import com.company.badgemate.service.ServerHeartbeatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/server-heartbeat")
@Tag(name = "Server Heartbeat", description = "API for handling server heartbeats")
public class ServerHeartbeatController {
    
    private final ServerHeartbeatService serverHeartbeatService;
    
    @Autowired
    public ServerHeartbeatController(ServerHeartbeatService serverHeartbeatService) {
        this.serverHeartbeatService = serverHeartbeatService;
    }
    
    @PostMapping
    @Operation(summary = "Process a server heartbeat", 
               description = "Accepts a server heartbeat and processes it")
    public ResponseEntity<String> processHeartbeat(@RequestBody ServerHeartbeat heartbeat) {
        try {
            serverHeartbeatService.processHeartbeat(heartbeat);
            return ResponseEntity.ok("Heartbeat processed successfully: " + heartbeat.getDeviceId());
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body("Error processing heartbeat: " + e.getMessage());
        }
    }
}
