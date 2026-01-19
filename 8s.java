package com.project.badgemate.controller;

import com.project.badgemate.dto.ServerHeartbeat;
import com.project.badgemate.dto.ServerHeartbeatResponse;
import com.project.badgemate.service.ServerHeartbeatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
    @Operation(
        summary = "Process a server heartbeat", 
        description = "Accepts a server heartbeat and responds with server status and timestamp_received"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Heartbeat processed successfully",
            content = @Content(schema = @Schema(implementation = ServerHeartbeatResponse.class))
        )
    })
    public ResponseEntity<ServerHeartbeatResponse> processHeartbeat(@RequestBody ServerHeartbeat heartbeat) {
        // Validate required fields
        if (heartbeat.getDeviceId() == null || heartbeat.getDeviceId().isEmpty()) {
            ServerHeartbeatResponse errorResponse = new ServerHeartbeatResponse();
            errorResponse.setDeviceId("unknown");
            errorResponse.setServerStatus("error");
            errorResponse.setTimestampReceived(System.currentTimeMillis() / 1000);
            errorResponse.setMessage("device_id is required");
            return ResponseEntity.badRequest().body(errorResponse);
        }
        
        // Process the heartbeat
        ServerHeartbeatResponse response = serverHeartbeatService.processHeartbeat(heartbeat);
        
        return ResponseEntity.ok(response);
    }
}
