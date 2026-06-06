package com.company.dataextract.model;

public class DatabaseConnectionConfig {
    private String name;
    private DatabaseType type;
    private String jdbcUrl;
    private String username;
    private String password;
    private String uri;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public DatabaseType getType() { return type; }
    public void setType(DatabaseType type) { this.type = type; }
    public String getJdbcUrl() { return jdbcUrl; }
    public void setJdbcUrl(String jdbcUrl) { this.jdbcUrl = jdbcUrl; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getUri() { return uri; }
    public void setUri(String uri) { this.uri = uri; }
}
