package com.project.badgemate.controller;

import com.project.badgemate.dto.EventLog;
import com.project.badgemate.dto.EventLogResponse;
import com.project.badgemate.service.EventLogService;
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
@RequestMapping("/api/event-log")
@Tag(name = "Event Log", description = "API for handling door access event logs with card validation")
public class EventLogController {
    
    private final EventLogService eventLogService;
    
    @Autowired
    public EventLogController(EventLogService eventLogService) {
        this.eventLogService = eventLogService;
    }
    
    @PostMapping
    @Operation(
        summary = "Process a door access event log", 
        description = "Validates card access using access_card, employee, and access_group_door tables. " +
                      "Returns access_granted or access_denied response. Accepts opened_at and closed_at timestamps " +
                      "to calculate open_seconds and avg_open_seconds. Automatically fetches door.lock_type from database."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Event log processed successfully",
            content = @Content(schema = @Schema(implementation = EventLogResponse.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid event log or validation error",
            content = @Content(schema = @Schema(implementation = EventLogResponse.class))
        )
    })
    public ResponseEntity<EventLogResponse> processEventLog(@RequestBody EventLog eventLog) {
        // Validate required fields
        String eventId = eventLog.getEventId();
        if (eventId == null || eventId.isEmpty()) {
            EventLogResponse errorResponse = new EventLogResponse();
            errorResponse.setEventId("unknown");
            errorResponse.setAccessStatus(EventLogResponse.AccessStatus.access_denied);
            errorResponse.setReason("event_id is required");
            errorResponse.setTimestamp(System.currentTimeMillis() / 1000);
            return ResponseEntity.badRequest().body(errorResponse);
        }
        
        if (eventLog.getCardHex() == null) {
            EventLogResponse errorResponse = new EventLogResponse();
            errorResponse.setEventId(eventId);
            errorResponse.setAccessStatus(EventLogResponse.AccessStatus.access_denied);
            errorResponse.setReason("card_hex is required");
            errorResponse.setTimestamp(System.currentTimeMillis() / 1000);
            return ResponseEntity.badRequest().body(errorResponse);
        }
        
        String doorId = eventLog.getDoorId();
        if (doorId == null || doorId.isEmpty()) {
            EventLogResponse errorResponse = new EventLogResponse();
            errorResponse.setEventId(eventId);
            errorResponse.setAccessStatus(EventLogResponse.AccessStatus.access_denied);
            errorResponse.setReason("door_id is required");
            errorResponse.setTimestamp(System.currentTimeMillis() / 1000);
            return ResponseEntity.badRequest().body(errorResponse);
        }
        
        // Process the event log
        EventLogResponse response = eventLogService.processEventLog(eventLog);
        
        // Return appropriate HTTP status based on access status
        if (response.getAccessStatus() == EventLogResponse.AccessStatus.access_granted) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
}
