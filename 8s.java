package com.company.badgemate.dto;

import lombok.Data;

@Data
public class CommandAcknowledgement {
    private String commandId;
    private String status; // applied, failed, pending
    private String reason;
    private Long timestamp;
    private Integer affectedRows;
}
