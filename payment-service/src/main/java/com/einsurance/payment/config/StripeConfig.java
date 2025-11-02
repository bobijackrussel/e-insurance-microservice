package com.einsurance.payment.config;

import com.stripe.Stripe;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * Stripe configuration
 * Initializes Stripe SDK with API keys
 */
@Slf4j
@Configuration
public class StripeConfig {

    @Value("${stripe.secret-key}")
    private String secretKey;

    @Value("${stripe.publishable-key}")
    private String publishableKey;

    @Value("${stripe.webhook-secret}")
    private String webhookSecret;

    @PostConstruct
    public void init() {
        Stripe.apiKey = secretKey;
        log.info("Stripe SDK initialized successfully");
        // Log environment mode without exposing keys
        boolean isTestMode = secretKey != null && secretKey.startsWith("sk_test");
        log.info("Stripe running in {} MODE", isTestMode ? "TEST" : "LIVE");
        if (isTestMode) {
            log.info("No real charges will be made in TEST mode");
        }
    }

    public String getSecretKey() {
        return secretKey;
    }

    public String getPublishableKey() {
        return publishableKey;
    }

    public String getWebhookSecret() {
        return webhookSecret;
    }
}