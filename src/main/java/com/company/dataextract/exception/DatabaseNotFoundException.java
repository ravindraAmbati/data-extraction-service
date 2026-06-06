package com.company.dataextract.exception;

public class DatabaseNotFoundException extends DataExtractionException {
    public DatabaseNotFoundException(String database) {
        super("DATABASE_NOT_FOUND", "Database configuration not found: " + database);
    }
}
