package com.company.dataextract.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.company.dataextract.exception.DataExtractionException;
import java.util.Arrays;
import org.junit.jupiter.api.Test;

class SqlIdentifierValidatorTest {
    @Test
    void acceptsSafeIdentifier() {
        assertEquals("schema.table", SqlIdentifierValidator.requireSafe("schema.table", "table"));
    }

    @Test
    void rejectsUnsafeIdentifier() {
        assertThrows(DataExtractionException.class,
                () -> SqlIdentifierValidator.requireSafe("employees;drop table employees", "table"));
    }

    @Test
    void buildsProjectionFromSafeColumns() {
        assertEquals("id, name", SqlIdentifierValidator.projection(Arrays.asList("id", "name")));
    }
}
