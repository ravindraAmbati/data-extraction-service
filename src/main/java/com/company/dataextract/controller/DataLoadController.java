package com.company.dataextract.controller;

import com.company.dataextract.dto.CollibraLoadResponse;
import com.company.dataextract.service.CollibraLoadService;
import io.swagger.v3.oas.annotations.Operation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/load")
public class DataLoadController {
    private static final Logger log = LoggerFactory.getLogger(DataLoadController.class);
    private final CollibraLoadService collibraLoadService;

    public DataLoadController(CollibraLoadService collibraLoadService) {
        this.collibraLoadService = collibraLoadService;
    }

    @PostMapping("/{database}/metadata")
    @Operation(summary = "Load transformed database metadata into Collibra")
    public CollibraLoadResponse loadDatabaseMetadata(@PathVariable String database) {
        log.info("Database metadata load request database={}", database);
        return collibraLoadService.loadDatabaseMetadata(database);
    }

    @PostMapping("/{database}/{schema}/metadata")
    @Operation(summary = "Load transformed schema metadata into Collibra")
    public CollibraLoadResponse loadSchemaMetadata(@PathVariable String database, @PathVariable String schema) {
        log.info("Schema metadata load request database={} schema={}", database, schema);
        return collibraLoadService.loadSchemaMetadata(database, schema);
    }

    @PostMapping("/{database}/{schema}/{table}/metadata")
    @Operation(summary = "Load transformed table metadata into Collibra")
    public CollibraLoadResponse loadTableMetadata(@PathVariable String database,
                                                  @PathVariable String schema,
                                                  @PathVariable String table) {
        log.info("Table metadata load request database={} schema={} table={}", database, schema, table);
        return collibraLoadService.loadTableMetadata(database, schema, table);
    }
}
