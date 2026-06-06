package com.company.dataextract.config;

import com.company.dataextract.model.DatabaseConnectionConfig;
import java.util.ArrayList;
import java.util.List;

public class DatabaseCatalog {
    private List<DatabaseConnectionConfig> databases = new ArrayList<>();

    public List<DatabaseConnectionConfig> getDatabases() {
        return databases;
    }

    public void setDatabases(List<DatabaseConnectionConfig> databases) {
        this.databases = databases == null ? new ArrayList<>() : databases;
    }
}
