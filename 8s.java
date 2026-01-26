package com.accesscontrol.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ValidateAccessRequest {
    private String controllerMac;
    private String readerUuid;
    private String cardUid;
    private String timestamp;
}
