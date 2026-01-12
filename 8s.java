package com.company.badgemate.controller;

import com.company.badgemate.dto.CommandAcknowledgement;
import com.company.badgemate.service.CommandAcknowledgementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/command-ack")
@Tag(name = "Command Acknowledgement", description = "API for handling command acknowledgements")
public class CommandAcknowledgementController {
    
    private final CommandAcknowledgementService commandAcknowledgementService;
    
    @Autowired
    public CommandAcknowledgementController(CommandAcknowledgementService commandAcknowledgementService) {
        this.commandAcknowledgementService = commandAcknowledgementService;
    }
    
    @PostMapping
    @Operation(summary = "Process a command acknowledgement", 
               description = "Accepts a command acknowledgement and processes it")
    public ResponseEntity<String> processAcknowledgement(@RequestBody CommandAcknowledgement acknowledgement) {
        try {
            commandAcknowledgementService.processAcknowledgement(acknowledgement);
            return ResponseEntity.ok("Acknowledgement processed successfully: " + acknowledgement.getCommandId());
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body("Error processing acknowledgement: " + e.getMessage());
        }
    }
}
