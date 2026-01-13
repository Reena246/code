package com.demo.accesscontrolsystem.dto;

import lombok.Data;

@Data
public class ValidateResponse {
    private boolean accessGranted;
    private String reason;
}
