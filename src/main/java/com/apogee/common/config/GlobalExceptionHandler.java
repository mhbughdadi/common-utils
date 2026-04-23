package com.apogee.common.config;

import com.apogee.common.exceptions.BusinessException;
import com.apogee.common.exceptions.DBException;
import com.apogee.common.exceptions.MapperException;
import com.apogee.common.exceptions.RecordNotFoundException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Global exception handler for handling common exceptions in Spring Boot applications.
 * This class provides centralized exception handling for controllers.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Map<String, Object>> handleBusinessException(BusinessException ex, WebRequest request) {
        logger.error("Business exception occurred: {}", ex.getMessage(), ex);
        Map<String, Object> body = createErrorResponse(ex.getErrorCode(), ex.getMessage(), HttpStatus.BAD_REQUEST);
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(DBException.class)
    public ResponseEntity<Map<String, Object>> handleDBException(DBException ex, WebRequest request) {
        logger.error("Database exception occurred: {}", ex.getMessage(), ex);
        Map<String, Object> body = createErrorResponse("DB_ERROR", ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(MapperException.class)
    public ResponseEntity<Map<String, Object>> handleMapperException(MapperException ex, WebRequest request) {
        logger.error("Mapper exception occurred: {}", ex.getMessage(), ex);
        Map<String, Object> body = createErrorResponse("MAPPER_ERROR", ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(RecordNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleRecordNotFoundException(RecordNotFoundException ex, WebRequest request) {
        logger.warn("Record not found: {}", ex.getMessage());
        Map<String, Object> body = createErrorResponse("NOT_FOUND", ex.getMessage(), HttpStatus.NOT_FOUND);
        return new ResponseEntity<>(body, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGlobalException(Exception ex, WebRequest request) {
        logger.error("Unexpected exception occurred: {}", ex.getMessage(), ex);
        Map<String, Object> body = createErrorResponse("INTERNAL_ERROR", "An unexpected error occurred", HttpStatus.INTERNAL_SERVER_ERROR);
        return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private Map<String, Object> createErrorResponse(String errorCode, String message, HttpStatus status) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("timestamp", LocalDateTime.now());
        errorResponse.put("status", status.value());
        errorResponse.put("error", status.getReasonPhrase());
        errorResponse.put("message", message);
        errorResponse.put("errorCode", errorCode);
        return errorResponse;
    }
}
