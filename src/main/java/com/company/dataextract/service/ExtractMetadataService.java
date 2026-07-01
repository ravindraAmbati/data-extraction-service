package com.company.dataextract.service;

import com.company.dataextract.dto.DatabaseMetadataExtractResponse;
import com.company.dataextract.dto.TableMetadataResponse;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ExtractMetadataService {
    private static final Logger log = LoggerFactory.getLogger(ExtractMetadataService.class);

    private final DataExtractionService dataExtractionService;
    private final ExtractFileStorageService storageService;

    public ExtractMetadataService(DataExtractionService dataExtractionService, ExtractFileStorageService storageService) {
        this.dataExtractionService = dataExtractionService;
        this.storageService = storageService;
    }

    public DatabaseMetadataExtractResponse extractDatabaseMetadata(String database) {
        String date = LocalDate.now().toString();
        Path outputDirectory = storageService.databaseDirectory(date, database);

        List<String> tables = dataExtractionService.listTables(database);
        Path tablesFile = storageService.writeJson(outputDirectory, "tables.json", tables);

        List<String> metadataFiles = new CopyOnWriteArrayList<>();
        List<String> failures = new CopyOnWriteArrayList<>();

        tables.parallelStream().forEach(table -> {
            try {
                TableMetadataResponse metadata = dataExtractionService.getMetadata(database, table);
                Path metadataFile = storageService.writeJson(outputDirectory, table + ".json", metadata);
                metadataFiles.add(metadataFile.toString());
            } catch (RuntimeException ex) {
                log.warn("Metadata extract failed for database={} table={}", database, table, ex);
                failures.add(table + ": " + ex.getMessage());
            }
        });

        Collections.sort(metadataFiles);
        Collections.sort(failures);

        DatabaseMetadataExtractResponse response = new DatabaseMetadataExtractResponse();
        response.setDatabase(database);
        response.setDate(date);
        response.setOutputDirectory(outputDirectory.toString());
        response.setTablesFile(tablesFile.toString());
        response.setTableCount(tables.size());
        response.setMetadataFiles(metadataFiles);
        response.setFailures(failures);
        return response;
    }
}
