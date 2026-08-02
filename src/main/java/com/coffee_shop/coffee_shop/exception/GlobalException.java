package com.coffee_shop.coffee_shop.exception;

import com.coffee_shop.coffee_shop.dto.response.ErrorResponse;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.time.format.DateTimeParseException;
import java.util.stream.Collectors;

@ControllerAdvice
public class GlobalException {

    // ==============================
    // Custom app exceptions
    // ==============================

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFoundException(ResourceNotFoundException ex) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorResponse> handleBadRequestException(BadRequestException ex) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalState(IllegalStateException ex) {
        return build(HttpStatus.CONFLICT, ex.getMessage());
    }

    // ==============================
    // Database
    // ==============================

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        return build(HttpStatus.CONFLICT, "A record with this value already exists.");
    }

    // ==============================
    // Request validation / parsing
    // ==============================

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .collect(Collectors.joining(", "));
        return build(HttpStatus.BAD_REQUEST, message.isBlank() ? "Validation failed" : message);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleUnreadableBody(HttpMessageNotReadableException ex) {
        return build(HttpStatus.BAD_REQUEST, "Malformed request body. Please check your JSON.");
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        return build(HttpStatus.BAD_REQUEST, "Invalid value for parameter '" + ex.getName() + "'");
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingParam(MissingServletRequestParameterException ex) {
        return build(HttpStatus.BAD_REQUEST, "Missing required parameter: " + ex.getParameterName());
    }

    @ExceptionHandler(NumberFormatException.class)
    public ResponseEntity<ErrorResponse> handleNumberFormat(NumberFormatException ex) {
        return build(HttpStatus.BAD_REQUEST, "Invalid numeric value in request.");
    }

    @ExceptionHandler(DateTimeParseException.class)
    public ResponseEntity<ErrorResponse> handleDateTimeParse(DateTimeParseException ex) {
        return build(HttpStatus.BAD_REQUEST, "Invalid date format. Expected yyyy-MM-dd HH:mm:ss.");
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    // ==============================
    // Routing
    // ==============================

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoHandlerFound(NoHandlerFoundException ex) {
        return build(HttpStatus.NOT_FOUND, "Endpoint not found: " + ex.getRequestURL());
    }

    // ==============================
    // Fallback (kept intentionally, do not remove)
    // ==============================

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Something went wrong. Please try again.");
    }

    private ResponseEntity<ErrorResponse> build(HttpStatus status, String message) {
        return new ResponseEntity<>(new ErrorResponse(status.value(), message), status);
    }
}