package com.company.dataextract.exception;

public class ApiDisabledException extends DataExtractionException {
    public ApiDisabledException(String path) {
        super("API_DISABLED", "API is disabled for ETL extract mode: " + path);
    }
}
