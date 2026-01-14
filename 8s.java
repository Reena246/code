package com.project.badgemate.service;

import com.project.badgemate.dto.CommandAcknowledgement;
import org.springframework.stereotype.Service;

@Service
public class CommandAckService {
    
    public void processCommandAcknowledgement(CommandAcknowledgement ack) {
        // Process the acknowledgment
        // In a real scenario, this would update command status, log, etc.
        System.out.println("Received acknowledgment for command: " + ack.getCommandId() + 
                          " with status: " + ack.getStatus());
    }
}
