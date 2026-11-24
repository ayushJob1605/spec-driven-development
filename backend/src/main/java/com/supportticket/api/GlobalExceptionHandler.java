package com.supportticket.api;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import com.supportticket.service.InvalidTransitionException;
import com.supportticket.service.ResourceNotFoundException;
import com.supportticket.service.ValidationException;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex, HttpServletRequest request) {
        return build(HttpStatus.NOT_FOUND, ErrorCodes.NOT_FOUND, ex.getMessage(), request.getRequestURI(), List.of());
    }

    @ExceptionHandler(InvalidTransitionException.class)
    public ResponseEntity<ErrorResponse> handleInvalidTransition(
            InvalidTransitionException ex, HttpServletRequest request) {
        return build(
                HttpStatus.CONFLICT,
                ErrorCodes.INVALID_TRANSITION,
                ex.getMessage(),
                request.getRequestURI(),
                List.of());
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorResponse> handleValidation(ValidationException ex, HttpServletRequest request) {
        return build(
                HttpStatus.UNPROCESSABLE_ENTITY,
                ErrorCodes.VALIDATION_ERROR,
                ex.getMessage(),
                request.getRequestURI(),
                ex.getFieldErrors());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        List<FieldErrorItem> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> new FieldErrorItem(error.getField(), error.getDefaultMessage()))
                .collect(Collectors.toList());
        return build(
                HttpStatus.UNPROCESSABLE_ENTITY,
                ErrorCodes.VALIDATION_ERROR,
                "Request validation failed",
                request.getRequestURI(),
                fieldErrors);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        String field = ex.getName() == null ? "parameter" : ex.getName();
        return build(
                HttpStatus.UNPROCESSABLE_ENTITY,
                ErrorCodes.VALIDATION_ERROR,
                "Invalid value for " + field,
                request.getRequestURI(),
                List.of(new FieldErrorItem(field, "invalid value")));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleNotReadable(
            HttpMessageNotReadableException ex, HttpServletRequest request) {
        Throwable cause = ex.getMostSpecificCause();
        if (cause instanceof UnrecognizedPropertyException unrecognized) {
            return build(
                    HttpStatus.BAD_REQUEST,
                    ErrorCodes.MALFORMED_REQUEST,
                    "Unknown property: " + unrecognized.getPropertyName(),
                    request.getRequestURI(),
                    List.of(new FieldErrorItem(unrecognized.getPropertyName(), "Unknown property")));
        }
        if (cause instanceof InvalidFormatException invalidFormat
                && invalidFormat.getTargetType() != null
                && invalidFormat.getTargetType().isEnum()) {
            String field = invalidFormat.getPath().isEmpty()
                    ? "value"
                    : invalidFormat.getPath().get(invalidFormat.getPath().size() - 1).getFieldName();
            return build(
                    HttpStatus.UNPROCESSABLE_ENTITY,
                    ErrorCodes.VALIDATION_ERROR,
                    "Invalid enum value",
                    request.getRequestURI(),
                    List.of(new FieldErrorItem(field == null ? "value" : field, "invalid enum value")));
        }
        return build(
                HttpStatus.BAD_REQUEST,
                ErrorCodes.MALFORMED_REQUEST,
                "Malformed request body",
                request.getRequestURI(),
                List.of());
    }

    private static ResponseEntity<ErrorResponse> build(
            HttpStatus status, String code, String message, String path, List<FieldErrorItem> fieldErrors) {
        ErrorResponse body = new ErrorResponse(Instant.now(), status.value(), code, message, path, fieldErrors);
        return ResponseEntity.status(status).body(body);
    }
}
