package com.company.dataextract.exception;

import com.company.dataextract.dto.ErrorResponse;
import javax.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(DataExtractionException.class)
    public ResponseEntity<ErrorResponse> handleDataExtraction(DataExtractionException ex, HttpServletRequest request) {
        log.warn("Request failed: {}", ex.getMessage(), ex);
        HttpStatus status = ex instanceof DatabaseNotFoundException || ex instanceof TableNotFoundException
                ? HttpStatus.NOT_FOUND : HttpStatus.BAD_REQUEST;
        if (ex instanceof EncryptionException) {
            status = HttpStatus.UNPROCESSABLE_ENTITY;
        }
        if (ex instanceof ApiDisabledException) {
            status = HttpStatus.GONE;
        }
        if (ex instanceof TransformException) {
            status = HttpStatus.UNPROCESSABLE_ENTITY;
        }
        return ResponseEntity.status(status).body(new ErrorResponse(ex.getCode(), ex.getMessage(), request.getRequestURI()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        return ResponseEntity.badRequest().body(new ErrorResponse("VALIDATION_FAILED", ex.getMessage(), request.getRequestURI()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception ex, HttpServletRequest request) {
        log.error("Unexpected request failure", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("INTERNAL_ERROR", "Unexpected error", request.getRequestURI()));
    }
}
