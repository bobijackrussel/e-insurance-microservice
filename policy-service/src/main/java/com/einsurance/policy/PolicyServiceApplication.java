package com.einsurance.policy;

import com.einsurance.common.config.OpenApiConfig;
import com.einsurance.common.config.SecurityConfig;
import com.einsurance.common.exception.GlobalExceptionHandler;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Policy Service Application
 * Manages insurance policy templates and customer policy purchases
 */
@SpringBootApplication
@EnableScheduling  // Enable scheduled tasks for policy expiry job
@ComponentScan(basePackages = {
    "com.einsurance.policy",
    "com.einsurance.common"
})
@Import({
    SecurityConfig.class,
    OpenApiConfig.class,
    GlobalExceptionHandler.class
})
public class PolicyServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(PolicyServiceApplication.class, args);
    }
}
