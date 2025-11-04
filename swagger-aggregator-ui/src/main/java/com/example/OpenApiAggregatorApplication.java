package com.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Boots the Swagger aggregator Spring application, enabling configuration binding and scheduled tasks.
 */
@SpringBootApplication
@ConfigurationPropertiesScan
@EnableScheduling
public class OpenApiAggregatorApplication {
    public static void main(String[] args) {
        SpringApplication.run(OpenApiAggregatorApplication.class, args);
    }
}
