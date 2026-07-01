package com.company.dataextract.strategy;

import com.company.dataextract.model.DatabaseType;
import com.company.dataextract.strategy.ibmdb2.IbmDb2DatabaseStrategy;
import com.company.dataextract.strategy.mongo.MongoDatabaseStrategy;
import com.company.dataextract.strategy.mssql.SqlServerDatabaseStrategy;
import com.company.dataextract.strategy.mysql.MySqlDatabaseStrategy;
import com.company.dataextract.strategy.oracle.OracleDatabaseStrategy;
import com.company.dataextract.strategy.postgres.PostgresDatabaseStrategy;
import com.company.dataextract.strategy.teradata.TeradataDatabaseStrategy;
import org.springframework.stereotype.Component;

@Component
public class DatabaseStrategyFactory {
    private final PostgresDatabaseStrategy postgres;
    private final MySqlDatabaseStrategy mysql;
    private final SqlServerDatabaseStrategy sqlServer;
    private final OracleDatabaseStrategy oracle;
    private final TeradataDatabaseStrategy teradata;
    private final IbmDb2DatabaseStrategy ibmDb2;
    private final MongoDatabaseStrategy mongo;

    public DatabaseStrategyFactory(PostgresDatabaseStrategy postgres, MySqlDatabaseStrategy mysql,
                                   SqlServerDatabaseStrategy sqlServer, OracleDatabaseStrategy oracle,
                                   TeradataDatabaseStrategy teradata, IbmDb2DatabaseStrategy ibmDb2,
                                   MongoDatabaseStrategy mongo) {
        this.postgres = postgres;
        this.mysql = mysql;
        this.sqlServer = sqlServer;
        this.oracle = oracle;
        this.teradata = teradata;
        this.ibmDb2 = ibmDb2;
        this.mongo = mongo;
    }

    public DatabaseStrategy resolve(DatabaseType type) {
        switch (type) {
            case POSTGRES:
                return postgres;
            case MYSQL:
                return mysql;
            case MSSQL:
                return sqlServer;
            case ORACLE:
                return oracle;
            case TERADATA:
                return teradata;
            case IBM_DB2:
                return ibmDb2;
            case MONGODB:
                return mongo;
            default:
                throw new IllegalArgumentException("Unsupported database type: " + type);
        }
    }
}
