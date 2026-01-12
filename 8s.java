package com.company.badgemate.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DatabaseCommand {
    
    @JsonProperty("command_id")
    private String commandId;
    
    @JsonProperty("command_type")
    private CommandType commandType;
    
    @JsonProperty("table_name")
    private String tableName;
    
    @JsonProperty("payload")
    private Map<String, Object> payload;
    
    @JsonProperty("timestamp")
    private Long timestamp;
    
    @JsonProperty("retry_count")
    private Integer retryCount = 0;
    
    public enum CommandType {
        INSERT, UPDATE, DELETE, SYNC, SYNC_RESPONSE
    }
}
