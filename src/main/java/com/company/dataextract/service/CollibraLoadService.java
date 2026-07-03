package com.company.dataextract.service;

import com.company.dataextract.config.CollibraProperties;
import com.company.dataextract.dto.CollibraLoadResponse;
import com.company.dataextract.exception.LoadException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.boot.web.client.RestTemplateBuilder;

@Service
public class CollibraLoadService {
    private final CollibraProperties collibraProperties;
    private final DataTransformService dataTransformService;
    private final RestTemplate restTemplate;

    public CollibraLoadService(CollibraProperties collibraProperties,
                               DataTransformService dataTransformService,
                               RestTemplateBuilder restTemplateBuilder) {
        this.collibraProperties = collibraProperties;
        this.dataTransformService = dataTransformService;
        this.restTemplate = restTemplateBuilder.build();
    }

    public CollibraLoadResponse loadDatabaseMetadata(String database) {
        dataTransformService.transformDatabaseMetadata(database);
        return load(dataTransformService.transformedDatabaseFile(database), database, null, null);
    }

    public CollibraLoadResponse loadSchemaMetadata(String database, String schema) {
        dataTransformService.transformSchemaMetadata(database, schema);
        return load(dataTransformService.transformedSchemaFile(database, schema), database, schema, null);
    }

    public CollibraLoadResponse loadTableMetadata(String database, String schema, String table) {
        dataTransformService.transformTableMetadata(database, schema, table);
        return load(dataTransformService.transformedTableFile(database, schema, table), database, schema, table);
    }

    public CollibraLoadResponse load(Path file, String database, String schema, String table) {
        try {
            if (!Files.exists(file)) {
                throw new LoadException("Transformed file does not exist: " + file, null);
            }
            String filename = file.getFileName().toString();
            byte[] bytes = Files.readAllBytes(file);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("filename", filename);
            body.add("file", jsonFileResource(filename, bytes));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
            if (hasCredentials()) {
                headers.setBasicAuth(collibraProperties.getUsername(), collibraProperties.getPassword());
            }

            String targetUrl = collibraProperties.url();
            ResponseEntity<String> response = restTemplate.postForEntity(targetUrl, new HttpEntity<>(body, headers), String.class);

            CollibraLoadResponse loadResponse = new CollibraLoadResponse();
            loadResponse.setFilename(filename);
            loadResponse.setSourcePath(file.toString());
            loadResponse.setReferredTransformJsons(Collections.singletonList(file.toString()));
            loadResponse.setReferredExtractJsons(referredExtractJsons(database, schema, table));
            loadResponse.setTargetUrl(targetUrl);
            loadResponse.setStatusCode(response.getStatusCodeValue());
            loadResponse.setResponseBody(response.getBody());
            return loadResponse;
        } catch (IOException | RuntimeException ex) {
            if (ex instanceof LoadException) {
                throw (LoadException) ex;
            }
            throw new LoadException("Failed to load Collibra file " + file, ex);
        }
    }

    public CollibraLoadResponse load(Path file) {
        return load(file, null, null, null);
    }

    private ByteArrayResource jsonFileResource(String filename, byte[] bytes) {
        return new ByteArrayResource(bytes) {
            @Override
            public String getFilename() {
                return filename;
            }

            @Override
            public long contentLength() {
                return bytes.length;
            }
        };
    }

    private boolean hasCredentials() {
        return collibraProperties.getUsername() != null && !collibraProperties.getUsername().isBlank()
                && collibraProperties.getPassword() != null && !collibraProperties.getPassword().isBlank();
    }

    private List<String> referredExtractJsons(String database, String schema, String table) {
        if (database == null) {
            return Collections.emptyList();
        }
        Path root = dataTransformService.extractTableFile(database, schema == null ? "default" : schema, table == null ? "metadata" : table)
                .getParent();
        if (schema == null) {
            root = root.getParent();
        }
        List<String> files = new ArrayList<>();
        try {
            if (table != null) {
                Path tableFile = dataTransformService.extractTableFile(database, schema, table);
                if (Files.exists(tableFile)) {
                    files.add(tableFile.toString());
                }
                return files;
            }
            if (Files.exists(root)) {
                Files.walk(root)
                        .filter(path -> Files.isRegularFile(path) && path.toString().endsWith(".json"))
                        .map(Path::toString)
                        .sorted()
                        .forEach(files::add);
            }
            return files;
        } catch (IOException ex) {
            throw new LoadException("Failed to collect referred extract JSON files", ex);
        }
    }
}
