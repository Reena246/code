package com.accesscontrol.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DbSyncResponse {
    private List<ReaderSync> readers;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReaderSync {
        private String readerUuid;
        private List<String> allowedCards;
    }
}
