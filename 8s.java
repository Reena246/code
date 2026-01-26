package com.accesscontrol.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DoorEventRequest {
    private String controllerMac;
    private String readerUuid;
    private String eventType;  // OPEN, CLOSE, FORCED
    private String timestamp;
}
