package com.einsurance.payment.service;

import com.einsurance.common.exception.PaymentException;
import com.stripe.model.Event;
import com.stripe.model.checkout.Session;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

/**
 * Stripe webhook event handler
 * Processes Stripe webhook events
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class StripeWebhookHandler {

    private final PaymentService paymentService;

    /**
     * Handle Stripe webhook event
     */
    public void handleWebhookEvent(Event event) {
        log.info("Processing Stripe webhook event: type={}, id={}", event.getType(), event.getId());

        switch (event.getType()) {
            case "checkout.session.completed":
                handleCheckoutSessionCompleted(event);
                break;

            case "checkout.session.async_payment_succeeded":
                handleCheckoutSessionCompleted(event);
                break;

            case "checkout.session.async_payment_failed":
                handleCheckoutSessionFailed(event);
                break;

            case "payment_intent.succeeded":
                handlePaymentIntentSucceeded(event);
                break;

            case "payment_intent.payment_failed":
                handlePaymentIntentFailed(event);
                break;

            default:
                log.debug("Unhandled webhook event type: {}", event.getType());
        }
    }

    /**
     * Handle successful checkout session
     */
    private void handleCheckoutSessionCompleted(Event event) {
        try {
            Session session = (Session) event.getDataObjectDeserializer()
                    .getObject()
                    .orElseThrow(() -> new PaymentException("Failed to deserialize session"));

            log.info("Checkout session completed: sessionId={}, paymentStatus={}", 
                    session.getId(), session.getPaymentStatus());

            // Extract metadata
            Map<String, String> metadata = session.getMetadata();
            UUID policyId = UUID.fromString(metadata.get("policyId"));

            // Process payment
            if ("paid".equals(session.getPaymentStatus())) {
                paymentService.handlePaymentSuccess(session.getId(), policyId);
            } else {
                log.warn("Payment status is not 'paid': {}", session.getPaymentStatus());
            }

        } catch (Exception e) {
            log.error("Error handling checkout.session.completed event", e);
            throw new PaymentException("Failed to process checkout session", e);
        }
    }

    /**
     * Handle failed checkout session
     */
    private void handleCheckoutSessionFailed(Event event) {
        try {
            Session session = (Session) event.getDataObjectDeserializer()
                    .getObject()
                    .orElseThrow(() -> new PaymentException("Failed to deserialize session"));

            log.warn("Checkout session failed: sessionId={}", session.getId());

            paymentService.handlePaymentFailure(session.getId(), "Async payment failed");

        } catch (Exception e) {
            log.error("Error handling checkout.session.async_payment_failed event", e);
        }
    }

    /**
     * Handle successful payment intent
     */
    private void handlePaymentIntentSucceeded(Event event) {
        log.info("Payment intent succeeded: eventId={}", event.getId());
        // Additional processing if needed
    }

    /**
     * Handle failed payment intent
     */
    private void handlePaymentIntentFailed(Event event) {
        log.warn("Payment intent failed: eventId={}", event.getId());
        // Additional processing if needed
    }
}