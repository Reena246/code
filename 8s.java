package com.demo.accesscontrolsystem.dto;

import lombok.Data;

@Data
public class ValidateRequest {
    private String cardUid;
    private Long doorId;
    private Long readerId;
    private String deviceId;
}
