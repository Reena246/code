package com.accesscontrol.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BulkEventLogsRequest {
    private String controllerMac;
    private List<EventLog> events;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EventLog {
        private String readerUuid;
        private String cardHex;
        private String eventType;
        private String eventTime;
    }
}
