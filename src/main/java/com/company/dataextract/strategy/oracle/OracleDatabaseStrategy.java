package com.company.dataextract.strategy.oracle;

import com.company.dataextract.dto.DataRequest;
import com.company.dataextract.repository.RelationalDataRepository;
import com.company.dataextract.service.ConnectionFactoryService;
import com.company.dataextract.strategy.AbstractRelationalDatabaseStrategy;
import org.springframework.stereotype.Component;

@Component
public class OracleDatabaseStrategy extends AbstractRelationalDatabaseStrategy {
    public OracleDatabaseStrategy(ConnectionFactoryService service, RelationalDataRepository repository) {
        super(service, repository);
    }

    @Override
    protected String buildRowsSql(String tableName, String projection, DataRequest request) {
        return "SELECT " + projection + " FROM " + tableName + orderBy(request) + " OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
    }

    @Override
    protected Object[] paginationArgs(DataRequest request) {
        return new Object[]{request.getOffset(), request.getLimit()};
    }
}
