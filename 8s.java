package com.demo.accesscontrolsystem.dto;

import lombok.Data;

@Data
public class ValidateRequest {
    private Long doorId;
    private String cardHex;
}
