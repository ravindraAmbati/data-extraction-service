package com.company.dataextract.dto;

import java.time.Instant;

public class ErrorResponse {
    private Instant timestamp = Instant.now();
    private String code;
    private String message;
    private String path;

    public ErrorResponse(String code, String message, String path) {
        this.code = code;
        this.message = message;
        this.path = path;
    }

    public Instant getTimestamp() { return timestamp; }
    public String getCode() { return code; }
    public String getMessage() { return message; }
    public String getPath() { return path; }
}
