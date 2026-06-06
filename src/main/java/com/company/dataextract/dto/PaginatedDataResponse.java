package com.company.dataextract.dto;

import java.util.List;
import java.util.Map;

public class PaginatedDataResponse {
    private String database;
    private String table;
    private long offset;
    private int limit;
    private long totalRows;
    private boolean hasNext;
    private List<Map<String, Object>> data;

    public PaginatedDataResponse() {
    }

    public PaginatedDataResponse(String database, String table, long offset, int limit, long totalRows,
                                 List<Map<String, Object>> data) {
        this.database = database;
        this.table = table;
        this.offset = offset;
        this.limit = limit;
        this.totalRows = totalRows;
        this.hasNext = offset + limit < totalRows;
        this.data = data;
    }

    public String getDatabase() { return database; }
    public void setDatabase(String database) { this.database = database; }
    public String getTable() { return table; }
    public void setTable(String table) { this.table = table; }
    public long getOffset() { return offset; }
    public void setOffset(long offset) { this.offset = offset; }
    public int getLimit() { return limit; }
    public void setLimit(int limit) { this.limit = limit; }
    public long getTotalRows() { return totalRows; }
    public void setTotalRows(long totalRows) { this.totalRows = totalRows; }
    public boolean isHasNext() { return hasNext; }
    public void setHasNext(boolean hasNext) { this.hasNext = hasNext; }
    public List<Map<String, Object>> getData() { return data; }
    public void setData(List<Map<String, Object>> data) { this.data = data; }
}
