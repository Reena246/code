package com.project.badgemate.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommandAcknowledgement {
    
    @JsonProperty("command_id")
    private String commandId;
    
    @JsonProperty("status")
    private String status; // applied, failed, pending
    
    @JsonProperty("reason")
    private String reason;
    
    @JsonProperty("timestamp")
    private Long timestamp;
    
    @JsonProperty("affected_rows")
    private Integer affectedRows;
}
