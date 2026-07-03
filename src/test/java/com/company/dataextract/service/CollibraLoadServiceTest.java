package com.company.dataextract.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.company.dataextract.config.CollibraProperties;
import com.company.dataextract.dto.CollibraLoadResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

class CollibraLoadServiceTest {
    @TempDir
    Path tempDir;

    @Test
    void postsJsonFileAndFilenameToCollibra() throws Exception {
        Path file = tempDir.resolve("metadata.json");
        Files.writeString(file, "[{\"resourceType\":\"Domain\"}]");

        CollibraProperties properties = new CollibraProperties();
        properties.setSsl(false);
        properties.setHost("collibra.local");
        properties.setPort(8080);
        CollibraProperties.Api api = new CollibraProperties.Api();
        api.setPath("/rest");
        api.setEndpoint("/import");
        properties.setApi(api);

        RestTemplate restTemplate = new RestTemplate();
        MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();
        server.expect(once(), requestTo("http://collibra.local:8080/rest/import"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().contentTypeCompatibleWith(MediaType.MULTIPART_FORM_DATA))
                .andRespond(withSuccess("{\"id\":\"job-1\"}", MediaType.APPLICATION_JSON));

        DataTransformService transformService = mock(DataTransformService.class);
        when(transformService.transformedDatabaseFile("postgres_hr")).thenReturn(file);
        when(transformService.transformDatabaseMetadata("postgres_hr"))
                .thenReturn(new com.company.dataextract.dto.FilePathResponse(
                        "TRANSFORM", "postgres_hr", null, null, java.util.Collections.singletonList(file.toString())));
        when(transformService.extractTableFile("postgres_hr", "default", "metadata")).thenReturn(file);

        CollibraLoadService service = new CollibraLoadService(
                properties,
                transformService,
                new RestTemplateBuilder().requestFactory(() -> restTemplate.getRequestFactory()));

        CollibraLoadResponse response = service.loadDatabaseMetadata("postgres_hr");

        assertEquals("metadata.json", response.getFilename());
        assertEquals(200, response.getStatusCode());
        assertEquals("{\"id\":\"job-1\"}", response.getResponseBody());
        server.verify();
    }
}
