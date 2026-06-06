package com.company.dataextract.exception;

public class EncryptionException extends DataExtractionException {
    public EncryptionException(String message, Throwable cause) {
        super("ENCRYPTION_ERROR", message, cause);
    }
}
