package com.einsurance.claims;

import com.einsurance.common.config.OpenApiConfig;
import com.einsurance.common.config.SecurityConfig;
import com.einsurance.common.exception.GlobalExceptionHandler;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Claims Service Application
 * Handles insurance claim submissions and approval workflow
 */
@SpringBootApplication
@ComponentScan(basePackages = {
    "com.einsurance.claims",
    "com.einsurance.common"
})
@Import({
    SecurityConfig.class,
    OpenApiConfig.class,
    GlobalExceptionHandler.class
})
public class ClaimsServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ClaimsServiceApplication.class, args);
    }

    @Bean
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder();
    }
}