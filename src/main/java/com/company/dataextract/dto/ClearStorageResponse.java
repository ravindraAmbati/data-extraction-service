package com.company.dataextract.dto;

public class ClearStorageResponse {
    private String database;
    private String schema;
    private String table;
    private String path;
    private boolean deleted;

    public ClearStorageResponse(String database, String schema, String table, String path, boolean deleted) {
        this.database = database;
        this.schema = schema;
        this.table = table;
        this.path = path;
        this.deleted = deleted;
    }

    public String getDatabase() { return database; }
    public String getSchema() { return schema; }
    public String getTable() { return table; }
    public String getPath() { return path; }
    public boolean isDeleted() { return deleted; }
}
