package com.company.badgemate.dto;

import lombok.Data;

@Data
public class ServerHeartbeatResponse {
    private String deviceId;
    private Long timestampReceived;
    private String serverStatus;
    private Long serverTimestamp;
}
