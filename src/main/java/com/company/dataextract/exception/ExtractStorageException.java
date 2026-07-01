package com.company.dataextract.exception;

public class ExtractStorageException extends DataExtractionException {
    public ExtractStorageException(String message, Throwable cause) {
        super("EXTRACT_STORAGE_ERROR", message, cause);
    }
}
