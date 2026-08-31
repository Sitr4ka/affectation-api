package com.micraft.org.affectpro.common;

import java.time.Instant;
import java.util.Map;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(ApiException.class)
    public ResponseEntity<Map<String, Object>> handleApi(ApiException e, HttpServletRequest request) {
        return response(e.getStatus(), e.getError(), e.getMessage(), request.getRequestURI());
    }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException e, HttpServletRequest request) {
        String message = e.getBindingResult().getFieldErrors().stream().findFirst().map(FieldError::getDefaultMessage).orElse("Requete invalide.");
        return response(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", message, request.getRequestURI());
    }
    private ResponseEntity<Map<String, Object>> response(HttpStatus status, String error, String message, String path) {
        return ResponseEntity.status(status).body(Map.of("timestamp", Instant.now().toString(), "status", status.value(), "error", error, "message", message, "path", path));
    }
}
