package com.rancho.api.common.dto;

import java.time.LocalDateTime;
import java.util.Map;

public record ApiErrorResponse(
        int status,
        String message,
        Map<String, String> errors,
        LocalDateTime timestamp
) {
    public ApiErrorResponse(int status, String message) {
        this(status, message, null, LocalDateTime.now());
    }

    public ApiErrorResponse(int status, String message, Map<String, String> errors) {
        this(status, message, errors, LocalDateTime.now());
    }
}
