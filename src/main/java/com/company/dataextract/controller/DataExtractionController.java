package com.company.dataextract.controller;

import com.company.dataextract.dto.DataRequest;
import com.company.dataextract.dto.PaginatedDataResponse;
import com.company.dataextract.dto.RowCountResponse;
import com.company.dataextract.dto.TableMetadataResponse;
import com.company.dataextract.service.DataExtractionService;
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

    public DataExtractionController(DataExtractionService service) {
        this.service = service;
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

    @GetMapping("/{database}/{table}/rowscount")
    @Operation(summary = "Get row count")
    public RowCountResponse rowCount(@PathVariable String database, @PathVariable String table) {
        log.info("Row count request database={} table={}", database, table);
        return service.getRowCount(database, table);
    }

    @GetMapping("/{database}/{table}/rows")
    @Operation(summary = "Get paginated rows")
    public PaginatedDataResponse rows(@PathVariable String database,
                                      @PathVariable String table,
                                      @RequestParam long offset,
                                      @RequestParam int limit,
                                      @RequestParam(required = false) List<String> columns,
                                      @RequestParam(required = false) String sortBy,
                                      @RequestParam(required = false) String sortOrder) {
        DataRequest request = new DataRequest();
        request.setOffset(offset);
        request.setLimit(limit);
        request.setColumns(columns);
        request.setSortBy(sortBy);
        request.setSortOrder(sortOrder);
        log.info("Rows request database={} table={} offset={} limit={}", database, table, offset, limit);
        return service.getRows(database, table, request);
    }
}
