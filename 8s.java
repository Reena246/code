package com.project.badgemate.service;

import com.project.badgemate.dto.ServerHeartbeat;
import com.project.badgemate.dto.ServerHeartbeatResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class ServerHeartbeatService {
    
    private static final Logger logger = LoggerFactory.getLogger(ServerHeartbeatService.class);
    
    public ServerHeartbeatResponse processHeartbeat(ServerHeartbeat heartbeat) {
        logger.debug("Processing server heartbeat from device: {} - Online: {}, Queue: {}", 
                    heartbeat.getDeviceId(), heartbeat.getIsOnline(), heartbeat.getQueueSize());
        
        long timestampReceived = Instant.now().getEpochSecond();
        
        // Log heartbeat information
        if (!Boolean.TRUE.equals(heartbeat.getIsOnline())) {
            logger.warn("Device {} reports offline status", heartbeat.getDeviceId());
        }
        
        if (heartbeat.getQueueSize() != null && heartbeat.getQueueSize() > 100) {
            logger.warn("Device {} has high queue size: {}", 
                       heartbeat.getDeviceId(), heartbeat.getQueueSize());
        }
        
        // Create response
        ServerHeartbeatResponse response = new ServerHeartbeatResponse();
        response.setDeviceId(heartbeat.getDeviceId());
        response.setServerStatus("online");
        response.setTimestampReceived(timestampReceived);
        response.setMessage("Heartbeat received successfully");
        
        // In a production system, you might want to:
        // - Update device status in database
        // - Alert if device is offline
        // - Monitor queue sizes
        // - Track uptime statistics
        
        return response;
    }
}
