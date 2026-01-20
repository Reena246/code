package com.company.badgemate.dto;

import lombok.Data;

@Data
public class EventLogResponse {
    private String eventId;
    private String eventType; // access_granted, access_denied
    private String doorType; // MAGNETIC, STRIKE
    private Long timestamp;
    private String message;
}
