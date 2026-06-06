package com.company.dataextract.strategy;

import com.company.dataextract.dto.DataRequest;
import com.company.dataextract.dto.PaginatedDataResponse;
import com.company.dataextract.dto.TableMetadataResponse;
import com.company.dataextract.model.DatabaseConnectionConfig;
import java.util.List;

public interface DatabaseStrategy {
    TableMetadataResponse getMetadata(DatabaseConnectionConfig config, String tableName);
    long getRowCount(DatabaseConnectionConfig config, String tableName);
    PaginatedDataResponse getRows(DatabaseConnectionConfig config, String tableName, DataRequest request);
    List<String> listTables(DatabaseConnectionConfig config);
}
