package com.company.dataextract.dto;

public class RowCountResponse {
    private String database;
    private String table;
    private long rowCount;

    public RowCountResponse(String database, String table, long rowCount) {
        this.database = database;
        this.table = table;
        this.rowCount = rowCount;
    }

    public String getDatabase() { return database; }
    public String getTable() { return table; }
    public long getRowCount() { return rowCount; }
}
