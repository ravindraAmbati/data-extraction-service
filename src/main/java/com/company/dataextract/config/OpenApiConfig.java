package com.company.dataextract.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI dataExtractionOpenApi() {
        return new OpenAPI().info(new Info()
                .title("Data Extraction Service API")
                .version("v1")
                .description("Standardized metadata, row count, and paginated extraction APIs."));
    }
}
