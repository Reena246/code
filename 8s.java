package com.project.badgemate.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventLogResponse {
    
    @JsonProperty("status")
    private String status; // access_granted, access_denied
    
    @JsonProperty("door_type")
    private String doorType; // MAGNETIC, STRIKE
    
    @JsonProperty("timestamp_sent")
    private Long timestampSent;
    
    @JsonProperty("opened_at")
    private Long openedAt;
    
    @JsonProperty("closed_at")
    private Long closedAt;
}
