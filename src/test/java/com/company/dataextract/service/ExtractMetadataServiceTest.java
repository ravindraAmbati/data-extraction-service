package com.company.dataextract.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.company.dataextract.config.DataExtractProperties;
import com.company.dataextract.dto.ColumnMetadata;
import com.company.dataextract.dto.DatabaseMetadataExtractResponse;
import com.company.dataextract.dto.TableMetadataResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ExtractMetadataServiceTest {
    @TempDir
    Path tempDir;

    @Test
    void extractsTablesAndMetadataToDatedDatabaseDirectory() {
        DataExtractionService dataExtractionService = mock(DataExtractionService.class);
        when(dataExtractionService.listTables("postgres_hr")).thenReturn(Arrays.asList("employees", "departments"));
        when(dataExtractionService.getMetadata("postgres_hr", "employees"))
                .thenReturn(metadata("postgres_hr", "employees", "id"));
        when(dataExtractionService.getMetadata("postgres_hr", "departments"))
                .thenReturn(metadata("postgres_hr", "departments", "department_id"));

        DataExtractProperties properties = new DataExtractProperties();
        properties.setExtractOutputRoot(tempDir.toString());
        ExtractFileStorageService storageService = new ExtractFileStorageService(new ObjectMapper(), properties);
        ExtractMetadataService service = new ExtractMetadataService(dataExtractionService, storageService);

        DatabaseMetadataExtractResponse response = service.extractDatabaseMetadata("postgres_hr");

        Path outputDir = tempDir.resolve(LocalDate.now().toString()).resolve("postgres_hr");
        assertEquals(2, response.getTableCount());
        assertTrue(Files.exists(outputDir.resolve("tables.json")));
        assertTrue(Files.exists(outputDir.resolve("employees.json")));
        assertTrue(Files.exists(outputDir.resolve("departments.json")));
        assertEquals(Collections.emptyList(), response.getFailures());
        assertEquals(2, response.getMetadataFiles().size());
    }

    private TableMetadataResponse metadata(String database, String table, String column) {
        return new TableMetadataResponse(database, table,
                Collections.singletonList(new ColumnMetadata(column, "BIGINT", false)));
    }
}
