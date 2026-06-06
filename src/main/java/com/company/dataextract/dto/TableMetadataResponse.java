package com.company.dataextract.dto;

import java.util.List;

public class TableMetadataResponse {
    private String database;
    private String table;
    private List<ColumnMetadata> columns;

    public TableMetadataResponse() {
    }

    public TableMetadataResponse(String database, String table, List<ColumnMetadata> columns) {
        this.database = database;
        this.table = table;
        this.columns = columns;
    }

    public String getDatabase() { return database; }
    public void setDatabase(String database) { this.database = database; }
    public String getTable() { return table; }
    public void setTable(String table) { this.table = table; }
    public List<ColumnMetadata> getColumns() { return columns; }
    public void setColumns(List<ColumnMetadata> columns) { this.columns = columns; }
}
