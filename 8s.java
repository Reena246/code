package com.company.badgemate.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventLog {
    
    @JsonProperty("event_id")
    private String eventId;
    
    @JsonProperty("event_type")
    private EventType eventType;
    
    @JsonProperty("door_id")
    private String doorId;
    
    @JsonProperty("card_hex")
    private Long cardHex;
    
    @JsonProperty("user_name")
    private String userName;
    
    @JsonProperty("details")
    private String details;
    
    @JsonProperty("timestamp")
    private Long timestamp;
    
    @JsonProperty("device_id")
    private String deviceId;
    
    public enum EventType {
        access_granted, access_denied, card_scan, system_event
    }
}
