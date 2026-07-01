package com.company.dataextract.dto;

import java.util.ArrayList;
import java.util.List;

public class DatabaseMetadataExtractResponse {
    private String database;
    private String date;
    private String outputDirectory;
    private String tablesFile;
    private int tableCount;
    private List<String> metadataFiles = new ArrayList<>();
    private List<String> failures = new ArrayList<>();

    public String getDatabase() {
        return database;
    }

    public void setDatabase(String database) {
        this.database = database;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getOutputDirectory() {
        return outputDirectory;
    }

    public void setOutputDirectory(String outputDirectory) {
        this.outputDirectory = outputDirectory;
    }

    public String getTablesFile() {
        return tablesFile;
    }

    public void setTablesFile(String tablesFile) {
        this.tablesFile = tablesFile;
    }

    public int getTableCount() {
        return tableCount;
    }

    public void setTableCount(int tableCount) {
        this.tableCount = tableCount;
    }

    public List<String> getMetadataFiles() {
        return metadataFiles;
    }

    public void setMetadataFiles(List<String> metadataFiles) {
        this.metadataFiles = metadataFiles;
    }

    public List<String> getFailures() {
        return failures;
    }

    public void setFailures(List<String> failures) {
        this.failures = failures;
    }
}
