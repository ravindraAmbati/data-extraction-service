package com.company.dataextract.service;

import com.company.dataextract.dto.ClearStorageResponse;
import java.util.Arrays;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class LocalStorageClearService {
    private final ExtractMetadataService extractMetadataService;
    private final DataTransformService dataTransformService;
    private final ExtractFileStorageService storageService;

    public LocalStorageClearService(ExtractMetadataService extractMetadataService,
                                    DataTransformService dataTransformService,
                                    ExtractFileStorageService storageService) {
        this.extractMetadataService = extractMetadataService;
        this.dataTransformService = dataTransformService;
        this.storageService = storageService;
    }

    public List<ClearStorageResponse> clearDatabase(String database) {
        return Arrays.asList(
                clear(database, null, null, extractMetadataService.extractDatabaseDirectory(database).toString()),
                clear(database, null, null, dataTransformService.transformDatabaseDirectory(database).toString()));
    }

    public List<ClearStorageResponse> clearSchema(String database, String schema) {
        return Arrays.asList(
                clear(database, schema, null, extractMetadataService.extractSchemaDirectory(database, schema).toString()),
                clear(database, schema, null, dataTransformService.transformSchemaDirectory(database, schema).toString()));
    }

    public List<ClearStorageResponse> clearTable(String database, String schema, String table) {
        return Arrays.asList(
                clear(database, schema, table, extractMetadataService.extractTableFile(database, schema, table).toString()),
                clear(database, schema, table, dataTransformService.transformedTableFile(database, schema, table).toString()));
    }

    private ClearStorageResponse clear(String database, String schema, String table, String path) {
        boolean deleted = storageService.delete(java.nio.file.Paths.get(path));
        return new ClearStorageResponse(database, schema, table, path, deleted);
    }
}
