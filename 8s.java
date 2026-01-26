package com.accesscontrol.exception;

public class AccessDeniedException extends RuntimeException {
    private final String reason;

    public AccessDeniedException(String message, String reason) {
        super(message);
        this.reason = reason;
    }

    public String getReason() {
        return reason;
    }
}
