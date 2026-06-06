package com.company.dataextract;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;

@SpringBootApplication(exclude = {
        DataSourceAutoConfiguration.class,
        MongoAutoConfiguration.class,
        MongoDataAutoConfiguration.class
})
public class DataExtractionServiceApplication {
    private static final Logger log = LoggerFactory.getLogger(DataExtractionServiceApplication.class);

    public static void main(String[] args) {
        log.info("Starting Data Extraction Service");
        SpringApplication.run(DataExtractionServiceApplication.class, args);
    }
}
