package com.company.dataextract.service;

import com.company.dataextract.config.DatabaseCatalog;
import com.company.dataextract.config.DataExtractProperties;
import com.company.dataextract.exception.ConnectionCreationException;
import com.company.dataextract.exception.DatabaseNotFoundException;
import com.company.dataextract.model.DatabaseConnectionConfig;
import com.company.dataextract.model.DatabaseType;
import com.company.dataextract.util.PasswordCryptoUtil;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import javax.annotation.PreDestroy;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.yaml.snakeyaml.Yaml;

@Service
public class ConnectionFactoryService {
    private static final Logger log = LoggerFactory.getLogger(ConnectionFactoryService.class);
    private final Map<String, DatabaseConnectionConfig> configs;
    private final Map<String, HikariDataSource> dataSources = new ConcurrentHashMap<>();
    private final Map<String, MongoClient> mongoClients = new ConcurrentHashMap<>();
    private final PasswordCryptoUtil passwordCryptoUtil;
    private final ResourceLoader resourceLoader;
    private final DataExtractProperties dataExtractProperties;
    private final Environment environment;

    public ConnectionFactoryService(PasswordCryptoUtil passwordCryptoUtil,
                                    ResourceLoader resourceLoader,
                                    DataExtractProperties dataExtractProperties,
                                    Environment environment) {
        this.passwordCryptoUtil = passwordCryptoUtil;
        this.resourceLoader = resourceLoader;
        this.dataExtractProperties = dataExtractProperties;
        this.environment = environment;
        this.configs = loadCatalog().getDatabases().stream()
                .peek(this::resolvePlaceholders)
                .collect(Collectors.toMap(DatabaseConnectionConfig::getName, config -> config));
        log.info("Loaded {} database connection configurations", configs.size());
    }

    public List<DatabaseConnectionConfig> listConnections() {
        return configs.values().stream().collect(Collectors.toList());
    }

    public DatabaseConnectionConfig getConfig(String name) {
        DatabaseConnectionConfig config = configs.get(name);
        if (config == null) {
            throw new DatabaseNotFoundException(name);
        }
        return config;
    }

    public DataSource getDataSource(DatabaseConnectionConfig config) {
        if (config.getType() == DatabaseType.MONGODB) {
            throw new ConnectionCreationException("MongoDB does not use JDBC DataSource", null);
        }
        return dataSources.computeIfAbsent(config.getName(), ignored -> createDataSource(config));
    }

    public MongoClient getMongoClient(DatabaseConnectionConfig config) {
        if (config.getType() != DatabaseType.MONGODB) {
            throw new ConnectionCreationException("Relational databases do not use MongoClient", null);
        }
        return mongoClients.computeIfAbsent(config.getName(), ignored -> MongoClients.create(config.getUri()));
    }

    private HikariDataSource createDataSource(DatabaseConnectionConfig config) {
        try {
            HikariConfig hikari = new HikariConfig();
            hikari.setPoolName(config.getName() + "-pool");
            hikari.setJdbcUrl(config.effectiveJdbcUrl());
            if (config.getDriverClassName() != null && !config.getDriverClassName().isBlank()) {
                hikari.setDriverClassName(config.getDriverClassName());
            }
            hikari.setUsername(config.getUsername());
            hikari.setPassword(passwordCryptoUtil.decryptIfEncrypted(config.getPassword()));
            if (config.getHikari() != null && config.getHikari().getSchema() != null) {
                hikari.setSchema(config.getHikari().getSchema());
            }
            if (config.getTomcat() != null && config.getTomcat().getValidationQuery() != null) {
                hikari.setConnectionTestQuery(config.getTomcat().getValidationQuery());
            }
            hikari.setMaximumPoolSize(10);
            hikari.setMinimumIdle(1);
            log.info("Creating Hikari datasource for {}", config.getName());
            return new HikariDataSource(hikari);
        } catch (RuntimeException ex) {
            throw new ConnectionCreationException("Failed to create connection pool for " + config.getName(), ex);
        }
    }

    private DatabaseCatalog loadCatalog() {
        try {
            Yaml yaml = new Yaml();
            Resource resource = resourceLoader.getResource(dataExtractProperties.getDbConfig());
            if (!resource.exists()) {
                resource = new ClassPathResource("dbConfig.yml");
            }
            Map<String, Object> raw = yaml.load(resource.getInputStream());
            return toCatalog(raw);
        } catch (IOException | RuntimeException ex) {
            log.warn("Database configuration could not be loaded. Application will start with no configured databases.", ex);
            return new DatabaseCatalog();
        }
    }

    @SuppressWarnings("unchecked")
    private DatabaseCatalog toCatalog(Map<String, Object> raw) {
        DatabaseCatalog catalog = new DatabaseCatalog();
        if (raw == null || raw.get("databases") == null) {
            return catalog;
        }
        List<DatabaseConnectionConfig> databases = new ArrayList<>();
        for (Map<String, Object> entry : (List<Map<String, Object>>) raw.get("databases")) {
            DatabaseConnectionConfig config = new DatabaseConnectionConfig();
            config.setName(text(entry.get("name")));
            config.setType(databaseType(text(entry.get("type"))));
            config.setJdbcUrl(text(entry.get("jdbcUrl")));
            config.setUrl(text(entry.get("url")));
            config.setUsername(text(entry.get("username")));
            config.setPassword(text(entry.get("password")));
            config.setUri(text(entry.get("uri")));
            config.setDriverClassName(text(first(entry, "driverClassName", "driver-class-name")));
            config.setMetadataQuery(text(first(entry, "metadataQuery", "metadata-query")));
            config.setDomainName(text(first(entry, "domainName", "domain-name")));
            config.setCommunityName(text(first(entry, "communityName", "community-name")));
            config.setTableAttributes(stringList(first(entry, "tableAttributes", "table-attributes")));
            config.setColumnAttributes(stringList(first(entry, "columnAttributes", "column-attributes")));

            Map<String, Object> hikari = nested(entry, "hikari");
            if (hikari != null) {
                DatabaseConnectionConfig.HikariSettings settings = new DatabaseConnectionConfig.HikariSettings();
                settings.setSchema(text(hikari.get("schema")));
                config.setHikari(settings);
            }

            Map<String, Object> tomcat = nested(entry, "tomcat");
            if (tomcat != null) {
                DatabaseConnectionConfig.TomcatSettings settings = new DatabaseConnectionConfig.TomcatSettings();
                settings.setValidationQuery(text(first(tomcat, "validationQuery", "validation-query")));
                config.setTomcat(settings);
            }
            databases.add(config);
        }
        catalog.setDatabases(databases);
        return catalog;
    }

    private Object first(Map<String, Object> source, String first, String second) {
        return source.containsKey(first) ? source.get(first) : source.get(second);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> nested(Map<String, Object> source, String key) {
        Object value = source.get(key);
        return value instanceof Map ? (Map<String, Object>) value : null;
    }

    private String text(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    @SuppressWarnings("unchecked")
    private List<String> stringList(Object value) {
        if (value == null) {
            return new ArrayList<>();
        }
        if (value instanceof List) {
            return ((List<Object>) value).stream().map(String::valueOf).collect(Collectors.toList());
        }
        return List.of(String.valueOf(value));
    }

    private DatabaseType databaseType(String value) {
        if (value == null) {
            throw new ConnectionCreationException("Database type is required", null);
        }
        return DatabaseType.valueOf(value.trim().replace('-', '_'));
    }

    private void resolvePlaceholders(DatabaseConnectionConfig config) {
        config.setJdbcUrl(resolve(config.getJdbcUrl()));
        config.setUrl(resolve(config.getUrl()));
        config.setUsername(resolve(config.getUsername()));
        config.setPassword(resolve(config.getPassword()));
        config.setUri(resolve(config.getUri()));
        config.setDriverClassName(resolve(config.getDriverClassName()));
        config.setMetadataQuery(resolve(config.getMetadataQuery()));
        config.setDomainName(resolve(config.getDomainName()));
        config.setCommunityName(resolve(config.getCommunityName()));
        config.setTableAttributes(config.getTableAttributes().stream().map(this::resolve).collect(Collectors.toList()));
        config.setColumnAttributes(config.getColumnAttributes().stream().map(this::resolve).collect(Collectors.toList()));
        if (config.getHikari() != null) {
            config.getHikari().setSchema(resolve(config.getHikari().getSchema()));
        }
        if (config.getTomcat() != null) {
            config.getTomcat().setValidationQuery(resolve(config.getTomcat().getValidationQuery()));
        }
    }

    private String resolve(String value) {
        return value == null ? null : environment.resolvePlaceholders(value);
    }

    @PreDestroy
    public void close() {
        dataSources.values().forEach(HikariDataSource::close);
        mongoClients.values().forEach(MongoClient::close);
        log.info("Closed cached database clients");
    }
}
