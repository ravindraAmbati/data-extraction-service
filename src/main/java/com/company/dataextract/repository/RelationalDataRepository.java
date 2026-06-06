package com.company.dataextract.repository;

import com.company.dataextract.dto.ColumnMetadata;
import com.company.dataextract.exception.DataExtractionException;
import com.company.dataextract.exception.TableNotFoundException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class RelationalDataRepository {
    private static final Logger log = LoggerFactory.getLogger(RelationalDataRepository.class);

    public List<ColumnMetadata> getColumns(DataSource dataSource, String tableName) {
        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metadata = connection.getMetaData();
            List<ColumnMetadata> columns = readColumns(metadata, tableName, null);
            if (columns.isEmpty() && tableName.contains(".")) {
                String[] parts = tableName.split("\\.", 2);
                columns = readColumns(metadata, parts[1], parts[0]);
            }
            if (columns.isEmpty()) {
                throw new TableNotFoundException(tableName);
            }
            return columns;
        } catch (SQLException ex) {
            throw new DataExtractionException("METADATA_FAILED", "Failed to retrieve metadata for " + tableName, ex);
        }
    }

    public long count(DataSource dataSource, String sql) {
        long start = System.currentTimeMillis();
        Long count = new JdbcTemplate(dataSource).queryForObject(sql, Long.class);
        log.info("Count query completed in {} ms", System.currentTimeMillis() - start);
        return count == null ? 0 : count;
    }

    public List<Map<String, Object>> rows(DataSource dataSource, String sql, Object... args) {
        long start = System.currentTimeMillis();
        List<Map<String, Object>> rows = new JdbcTemplate(dataSource).queryForList(sql, args);
        log.info("Rows query completed in {} ms", System.currentTimeMillis() - start);
        return rows;
    }

    private List<ColumnMetadata> readColumns(DatabaseMetaData metadata, String table, String schema) throws SQLException {
        List<ColumnMetadata> columns = new ArrayList<>();
        try (ResultSet rs = metadata.getColumns(null, schema, table, null)) {
            while (rs.next()) {
                columns.add(new ColumnMetadata(
                        rs.getString("COLUMN_NAME"),
                        rs.getString("TYPE_NAME"),
                        rs.getInt("NULLABLE") == DatabaseMetaData.columnNullable));
            }
        }
        return columns;
    }
}
