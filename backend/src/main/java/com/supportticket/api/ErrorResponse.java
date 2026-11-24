package com.supportticket.api;

import java.time.Instant;
import java.util.List;

public record ErrorResponse(
        Instant timestamp,
        int status,
        String code,
        String message,
        String path,
        List<FieldErrorItem> fieldErrors) {

    public ErrorResponse {
        if (fieldErrors == null) {
            fieldErrors = List.of();
        } else {
            fieldErrors = List.copyOf(fieldErrors);
        }
    }
}
