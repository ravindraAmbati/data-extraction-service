package com.company.dataextract.dto;

import java.util.List;

public class DataRequest {
    private long offset;
    private int limit;
    private List<String> columns;
    private String sortBy;
    private String sortOrder;

    public long getOffset() { return offset; }
    public void setOffset(long offset) { this.offset = offset; }
    public int getLimit() { return limit; }
    public void setLimit(int limit) { this.limit = limit; }
    public List<String> getColumns() { return columns; }
    public void setColumns(List<String> columns) { this.columns = columns; }
    public String getSortBy() { return sortBy; }
    public void setSortBy(String sortBy) { this.sortBy = sortBy; }
    public String getSortOrder() { return sortOrder; }
    public void setSortOrder(String sortOrder) { this.sortOrder = sortOrder; }
}
