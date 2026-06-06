package com.company.dataextract.util;

import com.company.dataextract.exception.DataExtractionException;
import java.util.Collection;
import java.util.stream.Collectors;

public final class SqlIdentifierValidator {
    private static final String IDENTIFIER = "[A-Za-z_][A-Za-z0-9_.$]*";

    private SqlIdentifierValidator() {
    }

    public static String requireSafe(String value, String label) {
        if (value == null || !value.matches(IDENTIFIER)) {
            throw new DataExtractionException("INVALID_IDENTIFIER", "Invalid " + label + ": " + value);
        }
        return value;
    }

    public static String projection(Collection<String> columns) {
        if (columns == null || columns.isEmpty()) {
            return "*";
        }
        return columns.stream()
                .map(column -> requireSafe(column, "column"))
                .collect(Collectors.joining(", "));
    }
}
