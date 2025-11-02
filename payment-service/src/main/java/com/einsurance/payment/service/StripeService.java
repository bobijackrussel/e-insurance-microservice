package com.einsurance.payment.service;

import com.einsurance.common.exception.PaymentException;
import com.einsurance.payment.config.StripeConfig;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import com.stripe.param.checkout.SessionCreateParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Stripe payment service
 * Handles Stripe API interactions for payment processing
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StripeService {

    private final StripeConfig stripeConfig;

    @Value("${stripe.success-url:http://localhost:3000/payment/success}")
    private String successUrl;

    @Value("${stripe.cancel-url:http://localhost:3000/payment/cancel}")
    private String cancelUrl;

    /**
     * Create Stripe Checkout Session
     */
    public Session createCheckoutSession(
            UUID userId,
            UUID policyId,
            BigDecimal amount,
            String policyName,
            String currency) {
        
        log.info("Creating Stripe checkout session for user: {}, policy: {}, amount: {} {}", 
                userId, policyId, amount, currency);

        try {
            // Convert amount to cents (Stripe uses smallest currency unit)
            long amountInCents = amount.multiply(new BigDecimal("100")).longValue();

            // Build metadata
            Map<String, String> metadata = new HashMap<>();
            metadata.put("userId", userId.toString());
            metadata.put("policyId", policyId.toString());
            metadata.put("policyName", policyName);

            // Create Stripe Checkout Session
            SessionCreateParams params = SessionCreateParams.builder()
                    .setMode(SessionCreateParams.Mode.PAYMENT)
                    .setSuccessUrl(successUrl + "?session_id={CHECKOUT_SESSION_ID}")
                    .setCancelUrl(cancelUrl)
                    .addLineItem(
                            SessionCreateParams.LineItem.builder()
                                    .setQuantity(1L)
                                    .setPriceData(
                                            SessionCreateParams.LineItem.PriceData.builder()
                                                    .setCurrency(currency.toLowerCase())
                                                    .setUnitAmount(amountInCents)
                                                    .setProductData(
                                                            SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                                    .setName("Insurance Policy: " + policyName)
                                                                    .setDescription("E-Insurance Policy Purchase")
                                                                    .build()
                                                    )
                                                    .build()
                                    )
                                    .build()
                    )
                    .putAllMetadata(metadata)
                    .build();

            Session session = Session.create(params);
            
            log.info("Stripe checkout session created successfully: {}", session.getId());
            return session;

        } catch (StripeException e) {
            log.error("Failed to create Stripe checkout session", e);
            throw new PaymentException("Failed to create payment session: " + e.getMessage(), e);
        }
    }

    /**
     * Retrieve Stripe Session by ID
     */
    public Session retrieveSession(String sessionId) {
        log.debug("Retrieving Stripe session: {}", sessionId);

        try {
            return Session.retrieve(sessionId);
        } catch (StripeException e) {
            log.error("Failed to retrieve Stripe session: {}", sessionId, e);
            throw new PaymentException("Failed to retrieve payment session: " + e.getMessage(), e);
        }
    }

    /**
     * Verify Stripe webhook signature
     */
    public Event constructEvent(String payload, String sigHeader) {
        log.debug("Verifying Stripe webhook signature");

        try {
            Event event = Webhook.constructEvent(
                    payload, 
                    sigHeader, 
                    stripeConfig.getWebhookSecret()
            );
            
            log.info("Webhook signature verified successfully. Event type: {}", event.getType());
            return event;

        } catch (Exception e) {
            log.error("Webhook signature verification failed", e);
            throw new PaymentException("Invalid webhook signature", e);
        }
    }

    /**
     * Get Stripe publishable key (for frontend)
     */
    public String getPublishableKey() {
        return stripeConfig.getPublishableKey();
    }
}