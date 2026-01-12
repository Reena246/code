package com.company.badgemate.controller;

import com.company.badgemate.dto.DatabaseCommand;
import com.company.badgemate.service.DatabaseCommandService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/database-command")
@Tag(name = "Database Command", description = "API for handling database commands")
public class DatabaseCommandController {
    
    private final DatabaseCommandService databaseCommandService;
    
    @Autowired
    public DatabaseCommandController(DatabaseCommandService databaseCommandService) {
        this.databaseCommandService = databaseCommandService;
    }
    
    @PostMapping
    @Operation(summary = "Process a database command", 
               description = "Accepts a database command and processes it")
    public ResponseEntity<String> processCommand(@RequestBody DatabaseCommand command) {
        try {
            databaseCommandService.processDatabaseCommand(command);
            return ResponseEntity.ok("Command processed successfully: " + command.getCommandId());
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body("Error processing command: " + e.getMessage());
        }
    }
}
