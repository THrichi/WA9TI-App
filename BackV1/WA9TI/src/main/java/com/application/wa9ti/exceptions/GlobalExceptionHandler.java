package com.application.wa9ti.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // Gestion des exceptions générales
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGeneralException(Exception ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST) // Code HTTP 400 (Bad Request)
                .body(Map.of("message", ex.getMessage())); // Structure JSON avec le message d'erreur
    }

    // Gestion des exceptions spécifiques (si nécessaire)
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgumentException(IllegalArgumentException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST) // Code HTTP 400 (Bad Request)
                .body(Map.of("message", ex.getMessage())); // Structure JSON avec le message d'erreur
    }

}
