package com.company.dataextract.dto;

public class ColumnMetadata {
    private String name;
    private String dataType;
    private boolean nullable;

    public ColumnMetadata() {
    }

    public ColumnMetadata(String name, String dataType, boolean nullable) {
        this.name = name;
        this.dataType = dataType;
        this.nullable = nullable;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDataType() { return dataType; }
    public void setDataType(String dataType) { this.dataType = dataType; }
    public boolean isNullable() { return nullable; }
    public void setNullable(boolean nullable) { this.nullable = nullable; }
}
