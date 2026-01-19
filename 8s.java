package com.project.badgemate.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventLogResponse {
    
    @JsonProperty("event_id")
    private String eventId;
    
    @JsonProperty("access_status")
    private AccessStatus accessStatus;
    
    @JsonProperty("reason")
    private String reason;
    
    @JsonProperty("timestamp")
    private Long timestamp;
    
    @JsonProperty("door_lock_type")
    private String doorLockType;  // Fetched from database
    
    public enum AccessStatus {
        access_granted, access_denied
    }
}
