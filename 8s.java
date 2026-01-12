package com.company.badgemate.service;

import com.company.badgemate.dto.ServerHeartbeat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ServerHeartbeatService {
    
    private static final Logger logger = LoggerFactory.getLogger(ServerHeartbeatService.class);
    
    public void processHeartbeat(ServerHeartbeat heartbeat) {
        logger.debug("Processing server heartbeat from device: {} - Online: {}, Queue: {}", 
                    heartbeat.getDeviceId(), heartbeat.getIsOnline(), heartbeat.getQueueSize());
        
        // Log heartbeat information
        if (!heartbeat.getIsOnline()) {
            logger.warn("Device {} reports offline status", heartbeat.getDeviceId());
        }
        
        if (heartbeat.getQueueSize() != null && heartbeat.getQueueSize() > 100) {
            logger.warn("Device {} has high queue size: {}", 
                       heartbeat.getDeviceId(), heartbeat.getQueueSize());
        }
        
        // In a production system, you might want to:
        // - Update device status in database
        // - Alert if device is offline
        // - Monitor queue sizes
        // - Track uptime statistics
    }
}
