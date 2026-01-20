package com.company.badgemate.dto;

import lombok.Data;
import java.util.Map;

@Data
public class DatabaseCommand {
    private String commandId;
    private String commandType; // INSERT, UPDATE, DELETE, SYNC, SYNC_RESPONSE
    private String tableName;
    private Map<String, Object> payload;
    private Long timestamp;
    private Integer retryCount;
}
