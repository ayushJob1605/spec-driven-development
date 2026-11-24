package com.supportticket.service;

import com.supportticket.api.FieldErrorItem;
import java.util.List;

public class ValidationException extends RuntimeException {

    private final List<FieldErrorItem> fieldErrors;

    public ValidationException(String message) {
        this(message, List.of());
    }

    public ValidationException(String message, List<FieldErrorItem> fieldErrors) {
        super(message);
        this.fieldErrors = fieldErrors == null ? List.of() : List.copyOf(fieldErrors);
    }

    public List<FieldErrorItem> getFieldErrors() {
        return fieldErrors;
    }
}
