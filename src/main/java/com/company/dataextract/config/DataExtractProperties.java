package com.company.dataextract.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "dataextract")
public class DataExtractProperties {
    private String dbConfig = "classpath:dbConfig.yml";
    private String extractOutputRoot = ".";

    public String getDbConfig() {
        return dbConfig;
    }

    public void setDbConfig(String dbConfig) {
        this.dbConfig = dbConfig;
    }

    public String getExtractOutputRoot() {
        return extractOutputRoot;
    }

    public void setExtractOutputRoot(String extractOutputRoot) {
        this.extractOutputRoot = extractOutputRoot;
    }
}
