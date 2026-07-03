package com.company.dataextract.service;

import com.company.dataextract.config.DataExtractProperties;
import com.company.dataextract.dto.ColumnMetadata;
import com.company.dataextract.dto.FilePathResponse;
import com.company.dataextract.dto.TableMetadataResponse;
import com.company.dataextract.exception.TransformException;
import com.company.dataextract.model.DatabaseConnectionConfig;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import org.springframework.stereotype.Service;

@Service
public class DataTransformService {
    private static final String DEFAULT_DOMAIN = "Source Data Dictionary";
    private static final String DEFAULT_COMMUNITY = "TO-Source";
    private static final String TABLE_TO_SCHEMA_RELATION = "F7ccc657-1232-4462-bd7f-2ae1e9124ea1:TARGET";
    private static final String COLUMN_TO_TABLE_RELATION = "00000000-0000-0000-0000-000000007042:TARGET";

    private final ObjectMapper objectMapper;
    private final DataExtractProperties dataExtractProperties;
    private final ConnectionFactoryService connectionFactoryService;
    private final ExtractMetadataService extractMetadataService;

    public DataTransformService(ObjectMapper objectMapper,
                                DataExtractProperties dataExtractProperties,
                                ConnectionFactoryService connectionFactoryService,
                                ExtractMetadataService extractMetadataService) {
        this.objectMapper = objectMapper;
        this.dataExtractProperties = dataExtractProperties;
        this.connectionFactoryService = connectionFactoryService;
        this.extractMetadataService = extractMetadataService;
    }

    public FilePathResponse transformDatabaseMetadata(String database) {
        extractMetadataService.extractDatabaseMetadata(database);
        Path databaseDirectory = extractDatabaseDirectory(database);
        List<String> tables = read(databaseDirectory.resolve("tables.json"), new TypeReference<List<String>>() {});
        List<String> files = new CopyOnWriteArrayList<>();
        List<Map<String, Object>> combined = new CopyOnWriteArrayList<>();
        tables.parallelStream()
                .map(table -> {
                    String schema = table != null && table.contains(".") ? schemaNameFromTable(table) : "default";
                    String simpleTable = simpleTableName(table);
                    Path file = writeTransformedTable(database, schema, simpleTable);
                    combined.addAll(read(file, new TypeReference<List<Map<String, Object>>>() {}));
                    return file.toString();
                })
                .forEach(files::add);
        Collections.sort(files);
        Path databaseFile = transformedDatabaseFile(database);
        write(databaseFile, combined);
        return new FilePathResponse("TRANSFORM", database, null, null, Collections.singletonList(databaseFile.toString()));
    }

    public FilePathResponse transformSchemaMetadata(String database, String schema) {
        extractMetadataService.extractSchemaMetadata(database, schema);
        List<String> tables = read(extractSchemaDirectory(database, schema).resolve("tables.json"), new TypeReference<List<String>>() {});
        List<String> files = new CopyOnWriteArrayList<>();
        List<Map<String, Object>> combined = new CopyOnWriteArrayList<>();
        tables.parallelStream()
                .map(table -> {
                    Path file = writeTransformedTable(database, schema, table);
                    combined.addAll(read(file, new TypeReference<List<Map<String, Object>>>() {}));
                    return file.toString();
                })
                .forEach(files::add);
        Collections.sort(files);
        Path schemaFile = transformSchemaDirectory(database, schema).resolve("metadata.json");
        write(schemaFile, combined);
        return new FilePathResponse("TRANSFORM", database, schema, null, Collections.singletonList(schemaFile.toString()));
    }

    public FilePathResponse transformTableMetadata(String database, String schema, String table) {
        extractMetadataService.extractTableMetadata(database, schema, table);
        Path outputFile = writeTransformedTable(database, schema, table);
        return new FilePathResponse("TRANSFORM", database, schema, table, Collections.singletonList(outputFile.toString()));
    }

    public List<Map<String, Object>> buildTableResources(String database, String schema, String table) {
        DatabaseConnectionConfig config = connectionFactoryService.getConfig(database);
        Path tableMetadataFile = extractTableFile(database, schema, table);
        TableMetadataResponse metadata = read(tableMetadataFile, new TypeReference<TableMetadataResponse>() {});

        String domainName = valueOrDefault(config.getDomainName(), DEFAULT_DOMAIN);
        String communityName = valueOrDefault(config.getCommunityName(), DEFAULT_COMMUNITY);
        String schemaName = schemaName(config, schema, metadata.getTable());
        String tableName = simpleTableName(metadata.getTable());
        String tableQualifiedName = schemaName + "." + tableName;

        List<Map<String, Object>> resources = new ArrayList<>();
        resources.add(domainResource(domainName, communityName));
        resources.add(schemaAsset(domainName, communityName, schemaName));
        resources.add(tableAsset(domainName, communityName, schemaName, tableName, metadata, config.getTableAttributes()));

        for (ColumnMetadata column : metadata.getColumns() == null ? Collections.<ColumnMetadata>emptyList() : metadata.getColumns()) {
            resources.add(columnAsset(domainName, communityName, tableQualifiedName, column, config.getColumnAttributes()));
        }
        return resources;
    }

    private Path writeTransformedTable(String database, String schema, String table) {
        List<Map<String, Object>> resources = buildTableResources(database, schema, table);
        Path file = transformedTableFile(database, schema, table);
        write(file, resources);
        return file;
    }

    private Map<String, Object> domainResource(String domainName, String communityName) {
        Map<String, Object> resource = new LinkedHashMap<>();
        resource.put("resourceType", "Domain");
        resource.put("identifier", mapOf(
                "name", domainName,
                "community", mapOf("name", communityName)));
        resource.put("type", mapOf("name", "Data Asset Domain"));
        return resource;
    }

    private Map<String, Object> schemaAsset(String domainName, String communityName, String schemaName) {
        Map<String, Object> resource = new LinkedHashMap<>();
        resource.put("resourceType", "Asset");
        resource.put("identifier", mapOf(
                "name", schemaName,
                "domain", domainIdentifier(domainName, communityName)));
        resource.put("type", mapOf("name", "Schema"));
        resource.put("attributes", attributes(mapOfString(
                "Schema Name", schemaName,
                "Schema Type", "Database Schema")));
        return resource;
    }

    private Map<String, Object> tableAsset(String domainName, String communityName, String schemaName, String tableName,
                                           TableMetadataResponse metadata, List<String> configuredAttributes) {
        Map<String, Object> resource = new LinkedHashMap<>();
        resource.put("resourceType", "Asset");
        resource.put("identifier", mapOf(
                "name", schemaName + "." + tableName,
                "domain", domainIdentifier(domainName, communityName)));
        resource.put("type", mapOf("name", "Table"));
        resource.put("attributes", attributes(tableAttributes(metadata, configuredAttributes)));
        resource.put("relations", mapOf(TABLE_TO_SCHEMA_RELATION,
                Collections.singletonList(mapOf(
                        "name", schemaName,
                        "domain", domainIdentifier(domainName, communityName)))));
        return resource;
    }

    private Map<String, Object> columnAsset(String domainName, String communityName, String tableQualifiedName,
                                            ColumnMetadata column, List<String> configuredAttributes) {
        Map<String, Object> resource = new LinkedHashMap<>();
        resource.put("resourceType", "Asset");
        resource.put("identifier", mapOf(
                "name", tableQualifiedName + "." + column.getName(),
                "domain", domainIdentifier(domainName, communityName)));
        resource.put("type", mapOf("name", "Column"));
        resource.put("attributes", attributes(columnAttributes(column, configuredAttributes)));
        resource.put("relations", mapOf(COLUMN_TO_TABLE_RELATION,
                Collections.singletonList(mapOf(
                        "name", tableQualifiedName,
                        "domain", domainIdentifier(domainName, communityName)))));
        return resource;
    }

    private Map<String, String> tableAttributes(TableMetadataResponse metadata, List<String> configuredAttributes) {
        Map<String, String> values = new LinkedHashMap<>();
        values.put("Table Type", "Table");
        values.put("TableKind", "Table");
        values.put("Column Count", String.valueOf(metadata.getColumns() == null ? 0 : metadata.getColumns().size()));
        values.put("Database", metadata.getDatabase());
        values.put("Table Name", metadata.getTable());
        includeConfigured(values, configuredAttributes);
        return values;
    }

    private Map<String, String> columnAttributes(ColumnMetadata column, List<String> configuredAttributes) {
        Map<String, String> values = new LinkedHashMap<>();
        values.put("Column Name", column.getName());
        values.put("Data Type", column.getDataType());
        values.put("DataType Description", column.getDataType());
        values.put("Nullable", String.valueOf(column.isNullable()));
        values.put("ColumnLength", "");
        values.put("ColumnFormat", column.getDataType());
        includeConfigured(values, configuredAttributes);
        return values;
    }

    private void includeConfigured(Map<String, String> values, List<String> configuredAttributes) {
        if (configuredAttributes == null) {
            return;
        }
        for (String attribute : configuredAttributes) {
            values.putIfAbsent(attribute, "");
        }
    }

    private Map<String, Object> attributes(Map<String, String> values) {
        Map<String, Object> attributes = new LinkedHashMap<>();
        values.forEach((name, value) -> attributes.put(name,
                Collections.singletonList(value == null || value.isEmpty() ? new LinkedHashMap<>() : mapOf("value", value))));
        return attributes;
    }

    private Map<String, Object> domainIdentifier(String domainName, String communityName) {
        return mapOf("name", domainName, "community", mapOf("name", communityName));
    }

    private String schemaName(DatabaseConnectionConfig config, String schema, String tableName) {
        if (schema != null && !schema.isBlank()) {
            return schema;
        }
        if (tableName != null && tableName.contains(".")) {
            return tableName.substring(0, tableName.lastIndexOf('.'));
        }
        if (config.getHikari() != null && config.getHikari().getSchema() != null && !config.getHikari().getSchema().isBlank()) {
            return config.getHikari().getSchema();
        }
        return config.getName();
    }

    private String simpleTableName(String tableName) {
        if (tableName != null && tableName.contains(".")) {
            return tableName.substring(tableName.lastIndexOf('.') + 1);
        }
        return tableName;
    }

    private Path extractDatabaseDirectory(String database) {
        return Paths.get(dataExtractProperties.getExtractOutputRoot(), LocalDate.now().toString(), safeFileName(database),
                        dataExtractProperties.getExtractOutputFolder())
                .toAbsolutePath()
                .normalize();
    }

    private Path extractSchemaDirectory(String database, String schema) {
        return extractDatabaseDirectory(database).resolve(safeFileName(schema)).toAbsolutePath().normalize();
    }

    public Path extractTableFile(String database, String schema, String table) {
        return extractSchemaDirectory(database, schema).resolve(safeFileName(table) + ".json").toAbsolutePath().normalize();
    }

    public Path transformDatabaseDirectory(String database) {
        return Paths.get(dataExtractProperties.getExtractOutputRoot(), LocalDate.now().toString(), safeFileName(database),
                        dataExtractProperties.getTransformOutputFolder())
                .toAbsolutePath()
                .normalize();
    }

    public Path transformSchemaDirectory(String database, String schema) {
        return transformDatabaseDirectory(database).resolve(safeFileName(schema)).toAbsolutePath().normalize();
    }

    public Path transformedTableFile(String database, String schema, String table) {
        return transformSchemaDirectory(database, schema).resolve(safeFileName(table) + ".json");
    }

    public Path transformedSchemaFile(String database, String schema) {
        return transformSchemaDirectory(database, schema).resolve("metadata.json");
    }

    public Path transformedDatabaseFile(String database) {
        return transformDatabaseDirectory(database).resolve("metadata.json");
    }

    private String schemaNameFromTable(String table) {
        return table.substring(0, table.lastIndexOf('.'));
    }

    private <T> T read(Path file, TypeReference<T> type) {
        try {
            return objectMapper.readValue(file.toFile(), type);
        } catch (IOException ex) {
            throw new TransformException("Failed to read transform input file " + file, ex);
        }
    }

    private void write(Path file, Object payload) {
        try {
            Files.createDirectories(file.getParent());
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(file.toFile(), payload);
        } catch (IOException ex) {
            throw new TransformException("Failed to write transformed metadata file " + file, ex);
        }
    }

    private String safeFileName(String value) {
        String safe = value == null ? "unknown" : value.replaceAll("[^A-Za-z0-9._-]", "_");
        return safe.isBlank() ? "unknown" : safe;
    }

    private String valueOrDefault(String value, String defaultValue) {
        return value == null || value.isBlank() ? defaultValue : value;
    }

    @SafeVarargs
    private final Map<String, Object> mapOf(Object... values) {
        Map<String, Object> map = new LinkedHashMap<>();
        for (int i = 0; i < values.length; i += 2) {
            map.put(String.valueOf(values[i]), values[i + 1]);
        }
        return map;
    }

    private Map<String, String> mapOfString(String key1, String value1, String key2, String value2) {
        Map<String, String> map = new LinkedHashMap<>();
        map.put(key1, value1);
        map.put(key2, value2);
        return map;
    }
}
