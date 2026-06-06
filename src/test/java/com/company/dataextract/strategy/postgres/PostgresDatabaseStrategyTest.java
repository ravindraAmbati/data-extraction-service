package com.company.dataextract.strategy.postgres;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

import com.company.dataextract.dto.DataRequest;
import com.company.dataextract.repository.RelationalDataRepository;
import com.company.dataextract.service.ConnectionFactoryService;
import org.junit.jupiter.api.Test;

class PostgresDatabaseStrategyTest {
    @Test
    void buildsLimitOffsetQuery() {
        TestStrategy strategy = new TestStrategy();
        DataRequest request = new DataRequest();
        request.setSortBy("id");
        request.setSortOrder("DESC");

        assertEquals("SELECT id, name FROM employees ORDER BY id DESC LIMIT ? OFFSET ?",
                strategy.sql("employees", "id, name", request));
    }

    private static class TestStrategy extends PostgresDatabaseStrategy {
        TestStrategy() {
            super(mock(ConnectionFactoryService.class), mock(RelationalDataRepository.class));
        }

        String sql(String table, String projection, DataRequest request) {
            return buildRowsSql(table, projection, request);
        }
    }
}
