package com.company.dataextract.repository;

import com.company.dataextract.dto.ColumnMetadata;
import com.company.dataextract.exception.DataExtractionException;
import com.company.dataextract.exception.TableNotFoundException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
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

    public List<ColumnMetadata> getColumnsByQuery(DataSource dataSource, String sql, String tableName) {
        try {
            long start = System.currentTimeMillis();
            List<Map<String, Object>> rows = queryForList(dataSource, sql, metadataQueryArgs(sql, tableName));
            log.info("Custom metadata query completed in {} ms", System.currentTimeMillis() - start);
            List<ColumnMetadata> columns = rows.stream()
                    .map(this::toColumnMetadata)
                    .collect(java.util.stream.Collectors.toList());
            if (columns.isEmpty()) {
                throw new TableNotFoundException(tableName);
            }
            return columns;
        } catch (RuntimeException ex) {
            if (ex instanceof TableNotFoundException) {
                throw ex;
            }
            throw new DataExtractionException("CUSTOM_METADATA_FAILED",
                    "Failed to retrieve metadata using custom query for " + tableName, ex);
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

    protected JdbcTemplate jdbcTemplate(DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    protected List<Map<String, Object>> queryForList(DataSource dataSource, String sql, Object... args) {
        return jdbcTemplate(dataSource).queryForList(sql, args);
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

    private ColumnMetadata toColumnMetadata(Map<String, Object> row) {
        Map<String, Object> normalized = normalize(row);
        String name = text(first(normalized, "name", "column_name", "columnname"));
        String dataType = text(first(normalized, "data_type", "datatype", "type_name", "type"));
        boolean nullable = nullable(first(normalized, "nullable", "is_nullable", "nulls"));
        return new ColumnMetadata(name, dataType, nullable);
    }

    private Map<String, Object> normalize(Map<String, Object> row) {
        Map<String, Object> normalized = new LinkedHashMap<>();
        row.forEach((key, value) -> normalized.put(key == null ? "" : key.toLowerCase().replaceAll("[^a-z0-9]", "_"), value));
        return normalized;
    }

    private Object first(Map<String, Object> row, String... keys) {
        for (String key : keys) {
            if (row.containsKey(key)) {
                return row.get(key);
            }
        }
        return null;
    }

    private String text(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private boolean nullable(Object value) {
        if (value == null) {
            return true;
        }
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        if (value instanceof Number) {
            return ((Number) value).intValue() != 0;
        }
        String text = String.valueOf(value);
        return "YES".equalsIgnoreCase(text)
                || "Y".equalsIgnoreCase(text)
                || "TRUE".equalsIgnoreCase(text)
                || "1".equals(text);
    }

    private String simpleTableName(String tableName) {
        if (tableName != null && tableName.contains(".")) {
            return tableName.substring(tableName.lastIndexOf('.') + 1);
        }
        return tableName;
    }

    private Object[] metadataQueryArgs(String sql, String tableName) {
        int count = placeholderCount(sql);
        if (count <= 0) {
            return new Object[0];
        }
        if (count == 1) {
            return new Object[]{tableName};
        }
        Object[] args = new Object[count];
        args[0] = tableName;
        args[1] = simpleTableName(tableName);
        for (int i = 2; i < count; i++) {
            args[i] = simpleTableName(tableName);
        }
        return args;
    }

    private int placeholderCount(String sql) {
        int count = 0;
        boolean inSingleQuote = false;
        for (int i = 0; i < sql.length(); i++) {
            char c = sql.charAt(i);
            if (c == '\'') {
                inSingleQuote = !inSingleQuote;
            } else if (c == '?' && !inSingleQuote) {
                count++;
            }
        }
        return count;
    }
}
