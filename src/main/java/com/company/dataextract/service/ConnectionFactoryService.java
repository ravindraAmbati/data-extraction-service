package com.company.dataextract.service;

import com.company.dataextract.config.DatabaseCatalog;
import com.company.dataextract.exception.ConnectionCreationException;
import com.company.dataextract.exception.DatabaseNotFoundException;
import com.company.dataextract.model.DatabaseConnectionConfig;
import com.company.dataextract.model.DatabaseType;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import javax.annotation.PreDestroy;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

@Service
public class ConnectionFactoryService {
    private static final Logger log = LoggerFactory.getLogger(ConnectionFactoryService.class);
    private final Map<String, DatabaseConnectionConfig> configs;
    private final Map<String, HikariDataSource> dataSources = new ConcurrentHashMap<>();
    private final Map<String, MongoClient> mongoClients = new ConcurrentHashMap<>();

    public ConnectionFactoryService() {
        this.configs = loadCatalog().getDatabases().stream()
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
            hikari.setJdbcUrl(config.getJdbcUrl());
            hikari.setUsername(config.getUsername());
            hikari.setPassword(config.getPassword());
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
            Yaml yaml = new Yaml(new Constructor(DatabaseCatalog.class));
            return yaml.load(new ClassPathResource("dbConfig.yml").getInputStream());
        } catch (IOException | RuntimeException ex) {
            throw new ConnectionCreationException("Failed to load dbConfig.yml", ex);
        }
    }

    @PreDestroy
    public void close() {
        dataSources.values().forEach(HikariDataSource::close);
        mongoClients.values().forEach(MongoClient::close);
        log.info("Closed cached database clients");
    }
}
