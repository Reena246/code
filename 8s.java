package com.company.badgemate.dto;

import lombok.Data;

@Data
public class EventLog {
    private String eventId;
    private String eventType; // card_scan, system_event, access_granted, access_denied
    private Long doorId;
    private String cardHex;
    private String userName;
    private String details;
    private Long timestamp;
    private String deviceId;
    private Long companyId;
}
