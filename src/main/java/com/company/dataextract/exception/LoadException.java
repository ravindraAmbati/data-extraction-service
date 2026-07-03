package com.company.dataextract.exception;

public class LoadException extends DataExtractionException {
    public LoadException(String message, Throwable cause) {
        super("LOAD_ERROR", message, cause);
    }
}
