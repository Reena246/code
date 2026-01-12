package com.company.badgemate.controller;

import com.company.badgemate.dto.EventLog;
import com.company.badgemate.service.EventLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/event-log")
@Tag(name = "Event Log", description = "API for handling event logs")
public class EventLogController {
    
    private final EventLogService eventLogService;
    
    @Autowired
    public EventLogController(EventLogService eventLogService) {
        this.eventLogService = eventLogService;
    }
    
    @PostMapping
    @Operation(summary = "Process an event log", 
               description = "Accepts an event log and stores it in the audit table")
    public ResponseEntity<String> processEventLog(@RequestBody EventLog eventLog) {
        try {
            eventLogService.processEventLog(eventLog);
            return ResponseEntity.ok("Event log processed successfully: " + eventLog.getEventId());
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body("Error processing event log: " + e.getMessage());
        }
    }
}
