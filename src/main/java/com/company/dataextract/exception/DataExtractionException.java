package com.company.dataextract.exception;

public class DataExtractionException extends RuntimeException {
    private final String code;

    public DataExtractionException(String code, String message) {
        super(message);
        this.code = code;
    }

    public DataExtractionException(String code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
