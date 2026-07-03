package com.company.dataextract.service;

import com.company.dataextract.dto.DatabaseMetadataExtractResponse;
import com.company.dataextract.dto.TableMetadataResponse;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;
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
                Path metadataFile = extractTableMetadata(database, schemaName(table), simpleTableName(table));
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

    public DatabaseMetadataExtractResponse extractSchemaMetadata(String database, String schema) {
        String date = LocalDate.now().toString();
        Path outputDirectory = storageService.schemaDirectory(date, database, schema);
        List<String> tables = dataExtractionService.listTables(database).stream()
                .filter(table -> belongsToSchema(table, schema))
                .map(this::simpleTableName)
                .collect(Collectors.toList());
        Path tablesFile = storageService.writeJson(outputDirectory, "tables.json", tables);

        List<String> metadataFiles = new CopyOnWriteArrayList<>();
        List<String> failures = new CopyOnWriteArrayList<>();
        tables.parallelStream().forEach(table -> {
            try {
                metadataFiles.add(extractTableMetadata(database, schema, table).toString());
            } catch (RuntimeException ex) {
                failures.add(schema + "." + table + ": " + ex.getMessage());
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

    public Path extractTableMetadata(String database, String schema, String table) {
        String date = LocalDate.now().toString();
        String qualifiedTable = schema + "." + table;
        TableMetadataResponse metadata = dataExtractionService.getMetadata(database, qualifiedTable);
        return storageService.writeJson(storageService.schemaDirectory(date, database, schema), table + ".json", metadata);
    }

    public Path extractDatabaseDirectory(String database) {
        return storageService.databaseDirectory(LocalDate.now().toString(), database);
    }

    public Path extractSchemaDirectory(String database, String schema) {
        return storageService.schemaDirectory(LocalDate.now().toString(), database, schema);
    }

    public Path extractTableFile(String database, String schema, String table) {
        return storageService.tableFile(LocalDate.now().toString(), database, schema, table);
    }

    private boolean belongsToSchema(String table, String schema) {
        return table != null && table.contains(".")
                ? table.substring(0, table.lastIndexOf('.')).equalsIgnoreCase(schema)
                : true;
    }

    private String schemaName(String table) {
        if (table != null && table.contains(".")) {
            return table.substring(0, table.lastIndexOf('.'));
        }
        return "default";
    }

    private String simpleTableName(String table) {
        if (table != null && table.contains(".")) {
            return table.substring(table.lastIndexOf('.') + 1);
        }
        return table;
    }
}
