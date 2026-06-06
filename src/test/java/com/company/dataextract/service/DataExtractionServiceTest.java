package com.company.dataextract.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.company.dataextract.config.PaginationProperties;
import com.company.dataextract.dto.DataRequest;
import com.company.dataextract.exception.InvalidPaginationException;
import com.company.dataextract.model.DatabaseConnectionConfig;
import com.company.dataextract.model.DatabaseType;
import com.company.dataextract.strategy.DatabaseStrategy;
import com.company.dataextract.strategy.DatabaseStrategyFactory;
import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DataExtractionServiceTest {
    private ConnectionFactoryService connectionFactoryService;
    private DatabaseStrategy strategy;
    private DataExtractionService service;

    @BeforeEach
    void setUp() {
        connectionFactoryService = mock(ConnectionFactoryService.class);
        DatabaseStrategyFactory strategyFactory = mock(DatabaseStrategyFactory.class);
        strategy = mock(DatabaseStrategy.class);
        PaginationProperties pagination = new PaginationProperties();
        pagination.setMaxLimit(10000);

        DatabaseConnectionConfig config = new DatabaseConnectionConfig();
        config.setName("postgres_hr");
        config.setType(DatabaseType.POSTGRES);

        when(connectionFactoryService.getConfig("postgres_hr")).thenReturn(config);
        when(connectionFactoryService.listConnections()).thenReturn(Collections.singletonList(config));
        when(strategyFactory.resolve(DatabaseType.POSTGRES)).thenReturn(strategy);

        service = new DataExtractionService(connectionFactoryService, strategyFactory, pagination);
    }

    @Test
    void listDatabasesReturnsConfiguredNames() {
        assertEquals(Collections.singletonList("postgres_hr"), service.listDatabases());
    }

    @Test
    void rejectsNegativeOffset() {
        DataRequest request = request(-1, 100);
        assertThrows(InvalidPaginationException.class, () -> service.getRows("postgres_hr", "employees", request));
    }

    @Test
    void rejectsZeroLimit() {
        DataRequest request = request(0, 0);
        assertThrows(InvalidPaginationException.class, () -> service.getRows("postgres_hr", "employees", request));
    }

    @Test
    void rejectsLimitAboveConfiguredMaximum() {
        DataRequest request = request(0, 10001);
        assertThrows(InvalidPaginationException.class, () -> service.getRows("postgres_hr", "employees", request));
    }

    private DataRequest request(long offset, int limit) {
        DataRequest request = new DataRequest();
        request.setOffset(offset);
        request.setLimit(limit);
        return request;
    }
}
