package com.project.badgemate.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ServerHeartbeatResponse {
    
    @JsonProperty("status")
    private String status;
    
    @JsonProperty("timestamp_received")
    private Long timestampReceived;
    
    @JsonProperty("message")
    private String message;
}
