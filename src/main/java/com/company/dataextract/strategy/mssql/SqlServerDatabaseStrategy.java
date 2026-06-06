package com.company.dataextract.strategy.mssql;

import com.company.dataextract.dto.DataRequest;
import com.company.dataextract.repository.RelationalDataRepository;
import com.company.dataextract.service.ConnectionFactoryService;
import com.company.dataextract.strategy.AbstractRelationalDatabaseStrategy;
import org.springframework.stereotype.Component;

@Component
public class SqlServerDatabaseStrategy extends AbstractRelationalDatabaseStrategy {
    public SqlServerDatabaseStrategy(ConnectionFactoryService service, RelationalDataRepository repository) {
        super(service, repository);
    }

    @Override
    protected String buildRowsSql(String tableName, String projection, DataRequest request) {
        String order = orderBy(request);
        if (order.isEmpty()) {
            order = " ORDER BY (SELECT NULL)";
        }
        return "SELECT " + projection + " FROM " + tableName + order + " OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
    }

    @Override
    protected Object[] paginationArgs(DataRequest request) {
        return new Object[]{request.getOffset(), request.getLimit()};
    }
}
