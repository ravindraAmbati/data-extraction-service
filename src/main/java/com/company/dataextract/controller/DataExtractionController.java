package com.company.dataextract.controller;

import com.company.dataextract.dto.DatabaseMetadataExtractResponse;
import com.company.dataextract.dto.PaginatedDataResponse;
import com.company.dataextract.dto.RowCountResponse;
import com.company.dataextract.dto.TableMetadataResponse;
import com.company.dataextract.exception.ApiDisabledException;
import com.company.dataextract.service.DataExtractionService;
import com.company.dataextract.service.ExtractMetadataService;
import io.swagger.v3.oas.annotations.Operation;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dataextract")
public class DataExtractionController {
    private static final Logger log = LoggerFactory.getLogger(DataExtractionController.class);
    private final DataExtractionService service;
    private final ExtractMetadataService extractMetadataService;

    public DataExtractionController(DataExtractionService service, ExtractMetadataService extractMetadataService) {
        this.service = service;
        this.extractMetadataService = extractMetadataService;
    }

    @GetMapping("/databases")
    @Operation(summary = "List configured databases")
    public List<String> listDatabases() {
        return service.listDatabases();
    }

    @GetMapping("/{database}/tables")
    @Operation(summary = "List tables or collections")
    public List<String> listTables(@PathVariable String database) {
        return service.listTables(database);
    }

    @GetMapping("/{database}/{table}/metadata")
    @Operation(summary = "Get table metadata")
    public TableMetadataResponse metadata(@PathVariable String database, @PathVariable String table) {
        log.info("Metadata request database={} table={}", database, table);
        return service.getMetadata(database, table);
    }

    @GetMapping("/{database}/metadata")
    @Operation(summary = "Extract all table metadata for a database to the filesystem")
    public DatabaseMetadataExtractResponse databaseMetadata(@PathVariable String database) {
        log.info("Database metadata extract request database={}", database);
        return extractMetadataService.extractDatabaseMetadata(database);
    }

    @GetMapping("/{database}/{table}/rowscount")
    @Operation(summary = "Disabled: row count API is not available in ETL extract mode")
    public RowCountResponse rowCount(@PathVariable String database, @PathVariable String table) {
        throw new ApiDisabledException("/api/dataextract/{database}/{table}/rowscount");
    }

    @GetMapping("/{database}/{table}/rows")
    @Operation(summary = "Disabled: paginated rows API is not available in ETL extract mode")
    public PaginatedDataResponse rows(@PathVariable String database,
                                      @PathVariable String table,
                                      @RequestParam long offset,
                                      @RequestParam int limit,
                                      @RequestParam(required = false) List<String> columns,
                                      @RequestParam(required = false) String sortBy,
                                      @RequestParam(required = false) String sortOrder) {
        throw new ApiDisabledException("/api/dataextract/{database}/{table}/rows");
    }
}
