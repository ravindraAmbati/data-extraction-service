package com.company.dataextract.service;

import com.company.dataextract.config.PaginationProperties;
import com.company.dataextract.dto.DataRequest;
import com.company.dataextract.dto.PaginatedDataResponse;
import com.company.dataextract.dto.RowCountResponse;
import com.company.dataextract.dto.TableMetadataResponse;
import com.company.dataextract.exception.InvalidPaginationException;
import com.company.dataextract.model.DatabaseConnectionConfig;
import com.company.dataextract.strategy.DatabaseStrategy;
import com.company.dataextract.strategy.DatabaseStrategyFactory;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class DataExtractionService {
    private final ConnectionFactoryService connectionFactoryService;
    private final DatabaseStrategyFactory strategyFactory;
    private final PaginationProperties paginationProperties;

    public DataExtractionService(ConnectionFactoryService connectionFactoryService,
                                 DatabaseStrategyFactory strategyFactory,
                                 PaginationProperties paginationProperties) {
        this.connectionFactoryService = connectionFactoryService;
        this.strategyFactory = strategyFactory;
        this.paginationProperties = paginationProperties;
    }

    public List<String> listDatabases() {
        return connectionFactoryService.listConnections().stream()
                .map(DatabaseConnectionConfig::getName)
                .collect(Collectors.toList());
    }

    public List<String> listTables(String database) {
        DatabaseConnectionConfig config = connectionFactoryService.getConfig(database);
        return strategy(config).listTables(config);
    }

    public TableMetadataResponse getMetadata(String database, String table) {
        DatabaseConnectionConfig config = connectionFactoryService.getConfig(database);
        return strategy(config).getMetadata(config, table);
    }

    public RowCountResponse getRowCount(String database, String table) {
        DatabaseConnectionConfig config = connectionFactoryService.getConfig(database);
        return new RowCountResponse(database, table, strategy(config).getRowCount(config, table));
    }

    public PaginatedDataResponse getRows(String database, String table, DataRequest request) {
        validatePagination(request);
        DatabaseConnectionConfig config = connectionFactoryService.getConfig(database);
        return strategy(config).getRows(config, table, request);
    }

    private void validatePagination(DataRequest request) {
        if (request.getOffset() < 0) {
            throw new InvalidPaginationException("offset must be greater than or equal to 0");
        }
        if (request.getLimit() <= 0) {
            throw new InvalidPaginationException("limit must be greater than 0");
        }
        if (request.getLimit() > paginationProperties.getMaxLimit()) {
            throw new InvalidPaginationException("limit must be less than or equal to " + paginationProperties.getMaxLimit());
        }
    }

    private DatabaseStrategy strategy(DatabaseConnectionConfig config) {
        return strategyFactory.resolve(config.getType());
    }
}
