package com.company.dataextract.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.company.dataextract.config.DataExtractProperties;
import com.company.dataextract.dto.ColumnMetadata;
import com.company.dataextract.dto.TableMetadataResponse;
import com.company.dataextract.model.DatabaseConnectionConfig;
import com.company.dataextract.model.DatabaseType;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class DataTransformServiceTest {
    @TempDir
    Path tempDir;

    @Test
    void transformsExtractedTableMetadataIntoDomainTableAndColumnAssets() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        Path databaseDir = tempDir.resolve(LocalDate.now().toString()).resolve("postgres_hr");
        Files.createDirectories(databaseDir);
        objectMapper.writeValue(databaseDir.resolve("tables.json").toFile(), Arrays.asList("employees"));
        objectMapper.writeValue(databaseDir.resolve("employees.json").toFile(),
                new TableMetadataResponse("postgres_hr", "employees",
                        Arrays.asList(new ColumnMetadata("id", "BIGINT", false))));

        DatabaseConnectionConfig config = new DatabaseConnectionConfig();
        config.setName("postgres_hr");
        config.setType(DatabaseType.POSTGRES);
        config.setDomainName("Source Data Dictionary");
        config.setCommunityName("TO-Source");
        config.setTableAttributes(Arrays.asList("Table Type", "TableKind"));
        config.setColumnAttributes(Arrays.asList("ColumnFormat", "DataType Description", "Nullable"));
        DatabaseConnectionConfig.HikariSettings hikari = new DatabaseConnectionConfig.HikariSettings();
        hikari.setSchema("public");
        config.setHikari(hikari);

        ConnectionFactoryService connectionFactoryService = mock(ConnectionFactoryService.class);
        when(connectionFactoryService.getConfig("postgres_hr")).thenReturn(config);

        DataExtractProperties properties = new DataExtractProperties();
        properties.setExtractOutputRoot(tempDir.toString());

        DataTransformService service = new DataTransformService(objectMapper, properties, connectionFactoryService);
        List<Map<String, Object>> resources = service.transformDatabaseMetadata("postgres_hr");

        assertEquals(3, resources.size());
        assertEquals("Domain", resources.get(0).get("resourceType"));
        assertEquals("Asset", resources.get(1).get("resourceType"));
        assertEquals("Asset", resources.get(2).get("resourceType"));

        Map<String, Object> tableIdentifier = cast(resources.get(1).get("identifier"));
        assertEquals("public.employees", tableIdentifier.get("name"));

        Map<String, Object> columnIdentifier = cast(resources.get(2).get("identifier"));
        assertEquals("public.employees.id", columnIdentifier.get("name"));

        Map<String, Object> columnAttributes = cast(resources.get(2).get("attributes"));
        assertTrue(columnAttributes.containsKey("DataType Description"));
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> cast(Object value) {
        return (Map<String, Object>) value;
    }
}
