package com.company.dataextract.service;

import com.company.dataextract.config.DataExtractProperties;
import com.company.dataextract.dto.ColumnMetadata;
import com.company.dataextract.dto.TableMetadataResponse;
import com.company.dataextract.exception.TransformException;
import com.company.dataextract.model.DatabaseConnectionConfig;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;
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

    public DataTransformService(ObjectMapper objectMapper,
                                DataExtractProperties dataExtractProperties,
                                ConnectionFactoryService connectionFactoryService) {
        this.objectMapper = objectMapper;
        this.dataExtractProperties = dataExtractProperties;
        this.connectionFactoryService = connectionFactoryService;
    }

    public List<Map<String, Object>> transformDatabaseMetadata(String database) {
        Path databaseDirectory = databaseDirectory(database);
        List<String> tables = read(databaseDirectory.resolve("tables.json"), new TypeReference<List<String>>() {});
        List<Map<String, Object>> transformed = new CopyOnWriteArrayList<>();
        tables.parallelStream()
                .map(table -> transformTableMetadata(database, table))
                .forEach(transformed::addAll);
        return new ArrayList<>(transformed);
    }

    public List<Map<String, Object>> transformTableMetadata(String database, String table) {
        DatabaseConnectionConfig config = connectionFactoryService.getConfig(database);
        Path tableMetadataFile = databaseDirectory(database).resolve(safeFileName(table) + ".json");
        TableMetadataResponse metadata = read(tableMetadataFile, new TypeReference<TableMetadataResponse>() {});

        String domainName = valueOrDefault(config.getDomainName(), DEFAULT_DOMAIN);
        String communityName = valueOrDefault(config.getCommunityName(), DEFAULT_COMMUNITY);
        String schemaName = schemaName(config, metadata.getTable());
        String tableName = simpleTableName(metadata.getTable());
        String tableQualifiedName = schemaName + "." + tableName;

        List<Map<String, Object>> resources = new ArrayList<>();
        resources.add(domainResource(domainName, communityName));
        resources.add(tableAsset(domainName, communityName, schemaName, tableName, metadata, config.getTableAttributes()));

        for (ColumnMetadata column : metadata.getColumns() == null ? Collections.<ColumnMetadata>emptyList() : metadata.getColumns()) {
            resources.add(columnAsset(domainName, communityName, tableQualifiedName, column, config.getColumnAttributes()));
        }
        return resources;
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

    private String schemaName(DatabaseConnectionConfig config, String tableName) {
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

    private Path databaseDirectory(String database) {
        return Paths.get(dataExtractProperties.getExtractOutputRoot(), LocalDate.now().toString(), safeFileName(database))
                .toAbsolutePath()
                .normalize();
    }

    private <T> T read(Path file, TypeReference<T> type) {
        try {
            return objectMapper.readValue(file.toFile(), type);
        } catch (IOException ex) {
            throw new TransformException("Failed to read transform input file " + file, ex);
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
}
