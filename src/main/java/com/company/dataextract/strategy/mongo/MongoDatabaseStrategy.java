package com.company.dataextract.strategy.mongo;

import com.company.dataextract.dto.ColumnMetadata;
import com.company.dataextract.dto.DataRequest;
import com.company.dataextract.dto.PaginatedDataResponse;
import com.company.dataextract.dto.TableMetadataResponse;
import com.company.dataextract.exception.TableNotFoundException;
import com.company.dataextract.model.DatabaseConnectionConfig;
import com.company.dataextract.service.ConnectionFactoryService;
import com.company.dataextract.strategy.DatabaseStrategy;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.bson.Document;
import org.springframework.stereotype.Component;

@Component
public class MongoDatabaseStrategy implements DatabaseStrategy {
    private final ConnectionFactoryService connectionFactoryService;

    public MongoDatabaseStrategy(ConnectionFactoryService connectionFactoryService) {
        this.connectionFactoryService = connectionFactoryService;
    }

    @Override
    public TableMetadataResponse getMetadata(DatabaseConnectionConfig config, String tableName) {
        Document sample = collection(config, tableName).find().first();
        if (sample == null) {
            return new TableMetadataResponse(config.getName(), tableName, new ArrayList<>());
        }
        List<ColumnMetadata> columns = sample.entrySet().stream()
                .map(entry -> new ColumnMetadata(entry.getKey(),
                        entry.getValue() == null ? "NULL" : entry.getValue().getClass().getSimpleName(),
                        true))
                .collect(Collectors.toList());
        return new TableMetadataResponse(config.getName(), tableName, columns);
    }

    @Override
    public long getRowCount(DatabaseConnectionConfig config, String tableName) {
        return collection(config, tableName).countDocuments();
    }

    @Override
    public PaginatedDataResponse getRows(DatabaseConnectionConfig config, String tableName, DataRequest request) {
        MongoCollection<Document> collection = collection(config, tableName);
        List<Map<String, Object>> data = new ArrayList<>();
        for (Document document : collection.find().skip(Math.toIntExact(request.getOffset())).limit(request.getLimit())) {
            data.add(document);
        }
        return new PaginatedDataResponse(config.getName(), tableName, request.getOffset(), request.getLimit(),
                collection.countDocuments(), data);
    }

    @Override
    public List<String> listTables(DatabaseConnectionConfig config) {
        List<String> names = new ArrayList<>();
        database(config).listCollectionNames().into(names);
        return names;
    }

    private MongoCollection<Document> collection(DatabaseConnectionConfig config, String collectionName) {
        MongoDatabase database = database(config);
        for (String existing : database.listCollectionNames()) {
            if (existing.equals(collectionName)) {
                return database.getCollection(collectionName);
            }
        }
        throw new TableNotFoundException(collectionName);
    }

    private MongoDatabase database(DatabaseConnectionConfig config) {
        String databaseName = config.getUri().substring(config.getUri().lastIndexOf('/') + 1);
        int queryStart = databaseName.indexOf('?');
        if (queryStart >= 0) {
            databaseName = databaseName.substring(0, queryStart);
        }
        return connectionFactoryService.getMongoClient(config).getDatabase(databaseName);
    }
}
