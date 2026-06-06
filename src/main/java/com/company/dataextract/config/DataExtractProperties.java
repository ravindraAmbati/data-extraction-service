package com.company.dataextract.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "dataextract")
public class DataExtractProperties {
    private String dbConfig = "classpath:dbConfig.yml";

    public String getDbConfig() {
        return dbConfig;
    }

    public void setDbConfig(String dbConfig) {
        this.dbConfig = dbConfig;
    }
}
