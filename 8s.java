package com.company.badgemate.service;

import com.company.badgemate.dto.CommandAcknowledgement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class CommandAcknowledgementService {
    
    private static final Logger logger = LoggerFactory.getLogger(CommandAcknowledgementService.class);
    
    public void processAcknowledgement(CommandAcknowledgement acknowledgement) {
        logger.info("Processing command acknowledgement: {} - Status: {}", 
                   acknowledgement.getCommandId(), acknowledgement.getStatus());
        
        // Log the acknowledgement
        switch (acknowledgement.getStatus()) {
            case applied:
                logger.info("Command {} successfully applied. Affected rows: {}", 
                           acknowledgement.getCommandId(), acknowledgement.getAffectedRows());
                break;
            case failed:
                logger.warn("Command {} failed. Reason: {}", 
                           acknowledgement.getCommandId(), acknowledgement.getReason());
                break;
            case pending:
                logger.info("Command {} is pending", acknowledgement.getCommandId());
                break;
        }
        
        // In a production system, you might want to:
        // - Update command status in database
        // - Notify waiting processes
        // - Retry failed commands
    }
}
