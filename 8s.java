package com.project.badgemate.service;

import com.project.badgemate.dto.ServerHeartbeat;
import com.project.badgemate.dto.ServerHeartbeatResponse;
import org.springframework.stereotype.Service;

@Service
public class ServerHeartbeatService {
    
    public ServerHeartbeatResponse processHeartbeat(ServerHeartbeat heartbeat) {
        ServerHeartbeatResponse response = new ServerHeartbeatResponse();
        response.setStatus("ok");
        response.setTimestampReceived(System.currentTimeMillis() / 1000);
        response.setMessage("Heartbeat received from device: " + heartbeat.getDeviceId());
        return response;
    }
}
