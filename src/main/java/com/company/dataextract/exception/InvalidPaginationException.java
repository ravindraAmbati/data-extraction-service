package com.company.dataextract.exception;

public class InvalidPaginationException extends DataExtractionException {
    public InvalidPaginationException(String message) {
        super("INVALID_PAGINATION", message);
    }
}
