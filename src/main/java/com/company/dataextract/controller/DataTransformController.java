package com.company.dataextract.controller;

import com.company.dataextract.dto.FilePathResponse;
import com.company.dataextract.service.DataTransformService;
import io.swagger.v3.oas.annotations.Operation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/transform")
public class DataTransformController {
    private static final Logger log = LoggerFactory.getLogger(DataTransformController.class);
    private final DataTransformService dataTransformService;

    public DataTransformController(DataTransformService dataTransformService) {
        this.dataTransformService = dataTransformService;
    }

    @GetMapping("/{database}/metadata")
    @Operation(summary = "Transform all extracted table metadata for a database")
    public FilePathResponse transformDatabaseMetadata(@PathVariable String database) {
        log.info("Database metadata transform request database={}", database);
        return dataTransformService.transformDatabaseMetadata(database);
    }

    @GetMapping("/{database}/{schema}/metadata")
    @Operation(summary = "Transform extracted schema metadata")
    public FilePathResponse transformSchemaMetadata(@PathVariable String database, @PathVariable String schema) {
        log.info("Schema metadata transform request database={} schema={}", database, schema);
        return dataTransformService.transformSchemaMetadata(database, schema);
    }

    @GetMapping("/{database}/{schema}/{table}/metadata")
    @Operation(summary = "Transform extracted table metadata")
    public FilePathResponse transformTableMetadata(@PathVariable String database,
                                                   @PathVariable String schema,
                                                   @PathVariable String table) {
        log.info("Table metadata transform request database={} schema={} table={}", database, schema, table);
        return dataTransformService.transformTableMetadata(database, schema, table);
    }
}
