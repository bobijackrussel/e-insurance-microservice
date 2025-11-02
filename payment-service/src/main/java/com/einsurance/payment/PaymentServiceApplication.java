package com.einsurance.payment;

import com.einsurance.common.config.OpenApiConfig;
import com.einsurance.common.config.SecurityConfig;
import com.einsurance.common.exception.GlobalExceptionHandler;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Payment Service Application
 * Handles payment processing with Stripe integration
 */
@SpringBootApplication
@EnableScheduling
@ComponentScan(basePackages = {
    "com.einsurance.payment",
    "com.einsurance.common"
})
@Import({
    SecurityConfig.class,
    OpenApiConfig.class,
    GlobalExceptionHandler.class
})
public class PaymentServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(PaymentServiceApplication.class, args);
    }

    @Bean
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder();
    }
}