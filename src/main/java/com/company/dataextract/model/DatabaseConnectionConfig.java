package com.company.dataextract.model;

import java.util.ArrayList;
import java.util.List;

public class DatabaseConnectionConfig {
    private String name;
    private DatabaseType type;
    private String jdbcUrl;
    private String url;
    private String username;
    private String password;
    private String uri;
    private String driverClassName;
    private String metadataQuery;
    private String domainName;
    private String communityName;
    private List<String> tableAttributes = new ArrayList<>();
    private List<String> columnAttributes = new ArrayList<>();
    private HikariSettings hikari;
    private TomcatSettings tomcat;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public DatabaseType getType() { return type; }
    public void setType(DatabaseType type) { this.type = type; }
    public String getJdbcUrl() { return jdbcUrl; }
    public void setJdbcUrl(String jdbcUrl) { this.jdbcUrl = jdbcUrl; }
    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
    public String effectiveJdbcUrl() { return jdbcUrl != null ? jdbcUrl : url; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getUri() { return uri; }
    public void setUri(String uri) { this.uri = uri; }
    public String getDriverClassName() { return driverClassName; }
    public void setDriverClassName(String driverClassName) { this.driverClassName = driverClassName; }
    public String getMetadataQuery() { return metadataQuery; }
    public void setMetadataQuery(String metadataQuery) { this.metadataQuery = metadataQuery; }
    public String getDomainName() { return domainName; }
    public void setDomainName(String domainName) { this.domainName = domainName; }
    public String getCommunityName() { return communityName; }
    public void setCommunityName(String communityName) { this.communityName = communityName; }
    public List<String> getTableAttributes() { return tableAttributes; }
    public void setTableAttributes(List<String> tableAttributes) {
        this.tableAttributes = tableAttributes == null ? new ArrayList<>() : tableAttributes;
    }
    public List<String> getColumnAttributes() { return columnAttributes; }
    public void setColumnAttributes(List<String> columnAttributes) {
        this.columnAttributes = columnAttributes == null ? new ArrayList<>() : columnAttributes;
    }
    public HikariSettings getHikari() { return hikari; }
    public void setHikari(HikariSettings hikari) { this.hikari = hikari; }
    public TomcatSettings getTomcat() { return tomcat; }
    public void setTomcat(TomcatSettings tomcat) { this.tomcat = tomcat; }

    public static class HikariSettings {
        private String schema;

        public String getSchema() { return schema; }
        public void setSchema(String schema) { this.schema = schema; }
    }

    public static class TomcatSettings {
        private String validationQuery;

        public String getValidationQuery() { return validationQuery; }
        public void setValidationQuery(String validationQuery) { this.validationQuery = validationQuery; }
    }
}
