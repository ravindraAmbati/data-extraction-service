package com.company.dataextract.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

import com.company.dataextract.dto.ColumnMetadata;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;
import org.junit.jupiter.api.Test;

class RelationalDataRepositoryTest {
    @Test
    void mapsCustomMetadataQueryRowsToColumns() {
        Map<String, Object> row = new HashMap<>();
        row.put("COLUMN_NAME", "id");
        row.put("DATA_TYPE", "BIGINT");
        row.put("IS_NULLABLE", "NO");

        RelationalDataRepository repository = new RelationalDataRepository() {
            @Override
            protected List<Map<String, Object>> queryForList(DataSource dataSource, String sql, Object... args) {
                assertEquals("select * from metadata where table_name = ?", sql);
                assertEquals("schema.employees", args[0]);
                return Collections.singletonList(row);
            }
        };

        List<ColumnMetadata> columns = repository.getColumnsByQuery(
                mock(DataSource.class),
                "select * from metadata where table_name = ?",
                "schema.employees");

        assertEquals(1, columns.size());
        assertEquals("id", columns.get(0).getName());
        assertEquals("BIGINT", columns.get(0).getDataType());
        assertEquals(false, columns.get(0).isNullable());
    }
}
