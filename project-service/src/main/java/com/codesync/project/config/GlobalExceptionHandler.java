package com.codesync.project.config;

import com.codesync.project.exception.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ProjectNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleProjectNotFound(ProjectNotFoundException ex, WebRequest request) {
        logger.error("Project not found: {}", ex.getMessage());
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage(), "ProjectNotFoundException");
    }

    @ExceptionHandler(UnauthorizedAccessException.class)
    public ResponseEntity<Map<String, Object>> handleUnauthorized(UnauthorizedAccessException ex, WebRequest request) {
        logger.error("Unauthorized access: {}", ex.getMessage());
        return buildResponse(HttpStatus.FORBIDDEN, ex.getMessage(), "UnauthorizedAccessException");
    }

    @ExceptionHandler(DuplicateProjectException.class)
    public ResponseEntity<Map<String, Object>> handleDuplicateProject(DuplicateProjectException ex, WebRequest request) {
        logger.error("Duplicate project: {}", ex.getMessage());
        return buildResponse(HttpStatus.CONFLICT, ex.getMessage(), "DuplicateProjectException");
    }

    @ExceptionHandler(InvalidOperationException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidOperation(InvalidOperationException ex, WebRequest request) {
        logger.error("Invalid operation: {}", ex.getMessage());
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), "InvalidOperationException");
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException ex, WebRequest request) {
        logger.error("Bad request: {}", ex.getMessage());
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), "IllegalArgumentException");
    }

    @ExceptionHandler(NullPointerException.class)
    public ResponseEntity<Map<String, Object>> handleNullPointer(NullPointerException ex, WebRequest request) {
        logger.error("Null pointer: {}", ex.getMessage());
        return buildResponse(HttpStatus.BAD_REQUEST, "Required field missing: " + ex.getMessage(), "NullPointerException");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex, WebRequest request) {
        logger.error("Error: {} - {}", ex.getClass().getName(), ex.getMessage(), ex);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, 
            ex.getMessage() != null ? ex.getMessage() : "Internal server error", 
            ex.getClass().getSimpleName());
    }

    private ResponseEntity<Map<String, Object>> buildResponse(HttpStatus status, String message, String type) {
        Map<String, Object> error = new HashMap<>();
        error.put("timestamp", LocalDateTime.now().toString());
        error.put("status", status.value());
        error.put("error", status.getReasonPhrase());
        error.put("message", message);
        error.put("type", type);
        return ResponseEntity.status(status).body(error);
    }
}
