package com.company.dataextract.controller;

import com.company.dataextract.service.DataTransformService;
import io.swagger.v3.oas.annotations.Operation;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/datatransform")
public class DataTransformController {
    private static final Logger log = LoggerFactory.getLogger(DataTransformController.class);
    private final DataTransformService dataTransformService;

    public DataTransformController(DataTransformService dataTransformService) {
        this.dataTransformService = dataTransformService;
    }

    @GetMapping("/{database}/metadata")
    @Operation(summary = "Transform all extracted table metadata for a database")
    public List<Map<String, Object>> transformDatabaseMetadata(@PathVariable String database) {
        log.info("Database metadata transform request database={}", database);
        return dataTransformService.transformDatabaseMetadata(database);
    }

    @GetMapping("/{database}/{table}/metadata")
    @Operation(summary = "Transform extracted table metadata")
    public List<Map<String, Object>> transformTableMetadata(@PathVariable String database, @PathVariable String table) {
        log.info("Table metadata transform request database={} table={}", database, table);
        return dataTransformService.transformTableMetadata(database, table);
    }
}
