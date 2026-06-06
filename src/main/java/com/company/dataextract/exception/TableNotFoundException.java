package com.company.dataextract.exception;

public class TableNotFoundException extends DataExtractionException {
    public TableNotFoundException(String table) {
        super("TABLE_NOT_FOUND", "Table or collection not found: " + table);
    }
}
