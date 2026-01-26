package com.accesscontrol.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ValidateAccessResponse {
    private String result;  // SUCCESS or DENIED
    private String lockType;  // MAGNETIC or STRIKE
    private String readerUuid;
    private String reason;
}
