package com.einsurance.user;

import com.einsurance.common.config.OpenApiConfig;
import com.einsurance.common.config.SecurityConfig;
import com.einsurance.common.exception.GlobalExceptionHandler;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;

/**
 * User Service Application
 * Manages user profiles and Keycloak integration
 */
@SpringBootApplication
@ComponentScan(basePackages = {
    "com.einsurance.user",
    "com.einsurance.common"
})
@Import({
    SecurityConfig.class,
    OpenApiConfig.class,
    GlobalExceptionHandler.class
})
public class UserServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(UserServiceApplication.class, args);
    }
}