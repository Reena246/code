package com.project.badgemate.dto;

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
    private String eventType;
    
    @JsonProperty("door_id")
    private Long doorId;
    
    @JsonProperty("card_hex")
    private String cardHex;
    
    @JsonProperty("user_name")
    private String userName;
    
    @JsonProperty("details")
    private String details;
    
    @JsonProperty("timestamp")
    private Long timestamp;
    
    @JsonProperty("device_id")
    private String deviceId;
    
    @JsonProperty("company_id")
    private Long companyId;
}
