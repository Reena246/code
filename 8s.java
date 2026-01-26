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
    private String cardHex;
    private String timestamp;
}
