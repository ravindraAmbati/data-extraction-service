package com.company.dataextract.strategy.postgres;

import com.company.dataextract.dto.DataRequest;
import com.company.dataextract.repository.RelationalDataRepository;
import com.company.dataextract.service.ConnectionFactoryService;
import com.company.dataextract.strategy.AbstractRelationalDatabaseStrategy;
import org.springframework.stereotype.Component;

@Component
public class PostgresDatabaseStrategy extends AbstractRelationalDatabaseStrategy {
    public PostgresDatabaseStrategy(ConnectionFactoryService service, RelationalDataRepository repository) {
        super(service, repository);
    }

    @Override
    protected String buildRowsSql(String tableName, String projection, DataRequest request) {
        return "SELECT " + projection + " FROM " + tableName + orderBy(request) + " LIMIT ? OFFSET ?";
    }
}
