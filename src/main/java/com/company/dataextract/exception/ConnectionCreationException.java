package com.company.dataextract.exception;

public class ConnectionCreationException extends DataExtractionException {
    public ConnectionCreationException(String message, Throwable cause) {
        super("CONNECTION_CREATION_FAILED", message, cause);
    }
}
