package com.company.dataextract.exception;

public class TransformException extends DataExtractionException {
    public TransformException(String message, Throwable cause) {
        super("TRANSFORM_ERROR", message, cause);
    }
}
