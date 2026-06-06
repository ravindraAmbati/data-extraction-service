package com.company.dataextract.strategy;

import com.company.dataextract.dto.DataRequest;
import com.company.dataextract.dto.PaginatedDataResponse;
import com.company.dataextract.dto.TableMetadataResponse;
import com.company.dataextract.model.DatabaseConnectionConfig;
import com.company.dataextract.repository.RelationalDataRepository;
import com.company.dataextract.service.ConnectionFactoryService;
import com.company.dataextract.util.SqlIdentifierValidator;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;

public abstract class AbstractRelationalDatabaseStrategy implements DatabaseStrategy {
    private final ConnectionFactoryService connectionFactoryService;
    private final RelationalDataRepository repository;

    protected AbstractRelationalDatabaseStrategy(ConnectionFactoryService connectionFactoryService,
                                                 RelationalDataRepository repository) {
        this.connectionFactoryService = connectionFactoryService;
        this.repository = repository;
    }

    @Override
    public TableMetadataResponse getMetadata(DatabaseConnectionConfig config, String tableName) {
        String safeTable = SqlIdentifierValidator.requireSafe(tableName, "table");
        return new TableMetadataResponse(config.getName(), tableName, repository.getColumns(dataSource(config), safeTable));
    }

    @Override
    public long getRowCount(DatabaseConnectionConfig config, String tableName) {
        String safeTable = SqlIdentifierValidator.requireSafe(tableName, "table");
        return repository.count(dataSource(config), "SELECT COUNT(*) FROM " + safeTable);
    }

    @Override
    public PaginatedDataResponse getRows(DatabaseConnectionConfig config, String tableName, DataRequest request) {
        String safeTable = SqlIdentifierValidator.requireSafe(tableName, "table");
        String projection = SqlIdentifierValidator.projection(request.getColumns());
        String sql = buildRowsSql(safeTable, projection, request);
        long totalRows = getRowCount(config, tableName);
        return new PaginatedDataResponse(config.getName(), tableName, request.getOffset(), request.getLimit(), totalRows,
                repository.rows(dataSource(config), sql, paginationArgs(request)));
    }

    @Override
    public List<String> listTables(DatabaseConnectionConfig config) {
        List<String> tables = new ArrayList<>();
        try (Connection connection = dataSource(config).getConnection();
             ResultSet rs = connection.getMetaData().getTables(null, null, "%", new String[]{"TABLE", "VIEW"})) {
            while (rs.next()) {
                tables.add(rs.getString("TABLE_NAME"));
            }
            return tables;
        } catch (SQLException ex) {
            throw new com.company.dataextract.exception.DataExtractionException("LIST_TABLES_FAILED",
                    "Failed to list tables for " + config.getName(), ex);
        }
    }

    protected String orderBy(DataRequest request) {
        if (request.getSortBy() == null || request.getSortBy().isBlank()) {
            return "";
        }
        String direction = "DESC".equalsIgnoreCase(request.getSortOrder()) ? " DESC" : " ASC";
        return " ORDER BY " + SqlIdentifierValidator.requireSafe(request.getSortBy(), "sortBy") + direction;
    }

    protected abstract String buildRowsSql(String tableName, String projection, DataRequest request);

    protected Object[] paginationArgs(DataRequest request) {
        return new Object[]{request.getLimit(), request.getOffset()};
    }

    private DataSource dataSource(DatabaseConnectionConfig config) {
        return connectionFactoryService.getDataSource(config);
    }
}
