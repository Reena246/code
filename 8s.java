package com.project.badgemate.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ServerHeartbeatResponse {
    
    @JsonProperty("device_id")
    private String deviceId;
    
    @JsonProperty("server_status")
    private String serverStatus;
    
    @JsonProperty("timestamp_received")
    private Long timestampReceived;
    
    @JsonProperty("message")
    private String message;
}
