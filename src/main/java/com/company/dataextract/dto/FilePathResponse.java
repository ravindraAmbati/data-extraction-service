package com.company.dataextract.dto;

import java.util.ArrayList;
import java.util.List;

public class FilePathResponse {
    private String stage;
    private String database;
    private String schema;
    private String table;
    private List<String> filePaths = new ArrayList<>();

    public FilePathResponse() {
    }

    public FilePathResponse(String stage, String database, String schema, String table, List<String> filePaths) {
        this.stage = stage;
        this.database = database;
        this.schema = schema;
        this.table = table;
        this.filePaths = filePaths;
    }

    public String getStage() { return stage; }
    public void setStage(String stage) { this.stage = stage; }
    public String getDatabase() { return database; }
    public void setDatabase(String database) { this.database = database; }
    public String getSchema() { return schema; }
    public void setSchema(String schema) { this.schema = schema; }
    public String getTable() { return table; }
    public void setTable(String table) { this.table = table; }
    public List<String> getFilePaths() { return filePaths; }
    public void setFilePaths(List<String> filePaths) { this.filePaths = filePaths; }
}
