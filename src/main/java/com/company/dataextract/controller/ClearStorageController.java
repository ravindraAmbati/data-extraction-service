package com.company.dataextract.controller;

import com.company.dataextract.dto.ClearStorageResponse;
import com.company.dataextract.service.LocalStorageClearService;
import io.swagger.v3.oas.annotations.Operation;
import java.util.List;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/clear")
public class ClearStorageController {
    private final LocalStorageClearService clearService;

    public ClearStorageController(LocalStorageClearService clearService) {
        this.clearService = clearService;
    }

    @DeleteMapping("/{database}/metadata")
    @Operation(summary = "Clear local extract and transform storage for a database")
    public List<ClearStorageResponse> clearDatabase(@PathVariable String database) {
        return clearService.clearDatabase(database);
    }

    @DeleteMapping("/{database}/{schema}/metadata")
    @Operation(summary = "Clear local extract and transform storage for a schema")
    public List<ClearStorageResponse> clearSchema(@PathVariable String database, @PathVariable String schema) {
        return clearService.clearSchema(database, schema);
    }

    @DeleteMapping("/{database}/{schema}/{table}/metadata")
    @Operation(summary = "Clear local extract and transform storage for a table")
    public List<ClearStorageResponse> clearTable(@PathVariable String database,
                                                 @PathVariable String schema,
                                                 @PathVariable String table) {
        return clearService.clearTable(database, schema, table);
    }
}
