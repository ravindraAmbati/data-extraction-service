package com.company.dataextract.service;

import com.company.dataextract.config.DataExtractProperties;
import com.company.dataextract.exception.ExtractStorageException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import org.springframework.stereotype.Service;

@Service
public class ExtractFileStorageService {
    private final ObjectMapper objectMapper;
    private final DataExtractProperties dataExtractProperties;

    public ExtractFileStorageService(ObjectMapper objectMapper, DataExtractProperties dataExtractProperties) {
        this.objectMapper = objectMapper;
        this.dataExtractProperties = dataExtractProperties;
    }

    public Path databaseDirectory(String date, String database) {
        return Paths.get(dataExtractProperties.getExtractOutputRoot(), date, safeFileName(database),
                        dataExtractProperties.getExtractOutputFolder())
                .toAbsolutePath()
                .normalize();
    }

    public Path schemaDirectory(String date, String database, String schema) {
        return databaseDirectory(date, database).resolve(safeFileName(schema)).toAbsolutePath().normalize();
    }

    public Path tableFile(String date, String database, String schema, String table) {
        return schemaDirectory(date, database, schema).resolve(safeFileName(table) + ".json").toAbsolutePath().normalize();
    }

    public Path writeJson(Path directory, String fileName, Object payload) {
        try {
            Files.createDirectories(directory);
            Path file = directory.resolve(safeFileName(fileName)).toAbsolutePath().normalize();
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(file.toFile(), payload);
            return file;
        } catch (IOException ex) {
            throw new ExtractStorageException("Failed to write extract file " + fileName, ex);
        }
    }

    private String safeFileName(String value) {
        String safe = value == null ? "unknown" : value.replaceAll("[^A-Za-z0-9._-]", "_");
        return safe.isBlank() ? "unknown" : safe;
    }

    public boolean delete(Path path) {
        if (!Files.exists(path)) {
            return false;
        }
        try {
            if (Files.isDirectory(path)) {
                Files.walk(path)
                        .sorted(Comparator.reverseOrder())
                        .forEach(this::deleteSingle);
            } else {
                Files.deleteIfExists(path);
            }
            return true;
        } catch (IOException ex) {
            throw new ExtractStorageException("Failed to clear local storage path " + path, ex);
        }
    }

    private void deleteSingle(Path path) {
        try {
            Files.deleteIfExists(path);
        } catch (IOException ex) {
            throw new ExtractStorageException("Failed to delete " + path, ex);
        }
    }
}
