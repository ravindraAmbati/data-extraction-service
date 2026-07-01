package com.company.dataextract.strategy;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;

import com.company.dataextract.model.DatabaseType;
import com.company.dataextract.strategy.ibmdb2.IbmDb2DatabaseStrategy;
import com.company.dataextract.strategy.mongo.MongoDatabaseStrategy;
import com.company.dataextract.strategy.mssql.SqlServerDatabaseStrategy;
import com.company.dataextract.strategy.mysql.MySqlDatabaseStrategy;
import com.company.dataextract.strategy.oracle.OracleDatabaseStrategy;
import com.company.dataextract.strategy.postgres.PostgresDatabaseStrategy;
import com.company.dataextract.strategy.teradata.TeradataDatabaseStrategy;
import org.junit.jupiter.api.Test;

class DatabaseStrategyFactoryTest {
    @Test
    void resolvesEachDatabaseType() {
        PostgresDatabaseStrategy postgres = mock(PostgresDatabaseStrategy.class);
        MySqlDatabaseStrategy mysql = mock(MySqlDatabaseStrategy.class);
        SqlServerDatabaseStrategy sqlServer = mock(SqlServerDatabaseStrategy.class);
        OracleDatabaseStrategy oracle = mock(OracleDatabaseStrategy.class);
        TeradataDatabaseStrategy teradata = mock(TeradataDatabaseStrategy.class);
        IbmDb2DatabaseStrategy ibmDb2 = mock(IbmDb2DatabaseStrategy.class);
        MongoDatabaseStrategy mongo = mock(MongoDatabaseStrategy.class);

        DatabaseStrategyFactory factory = new DatabaseStrategyFactory(postgres, mysql, sqlServer, oracle, teradata, ibmDb2, mongo);

        assertSame(postgres, factory.resolve(DatabaseType.POSTGRES));
        assertSame(mysql, factory.resolve(DatabaseType.MYSQL));
        assertSame(sqlServer, factory.resolve(DatabaseType.MSSQL));
        assertSame(oracle, factory.resolve(DatabaseType.ORACLE));
        assertSame(teradata, factory.resolve(DatabaseType.TERADATA));
        assertSame(ibmDb2, factory.resolve(DatabaseType.IBM_DB2));
        assertSame(mongo, factory.resolve(DatabaseType.MONGODB));
    }
}
