package com.company.badgemate.dto;

import lombok.Data;

@Data
public class ServerHeartbeat {
    private String deviceId;
    private Long timestamp;
    private Boolean isOnline;
    private Integer queueSize;
    private String dbVersionHash;
    private Integer uptimeSeconds;
}
