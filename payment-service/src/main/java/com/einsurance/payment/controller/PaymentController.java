package com.einsurance.payment.controller;

import com.einsurance.common.dto.ApiResponse;
import com.einsurance.common.dto.CheckoutSessionRequest;
import com.einsurance.common.dto.CheckoutSessionResponse;
import com.einsurance.common.dto.PageResponse;
import com.einsurance.common.dto.TransactionDto;
import com.einsurance.payment.service.PaymentService;
import com.einsurance.payment.service.StripeService;
import com.einsurance.payment.service.StripeWebhookHandler;
import com.einsurance.payment.service.TransactionStatistics;
import com.stripe.model.Event;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * REST controller for Payment operations
 */
@Slf4j
@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Tag(name = "Payments", description = "APIs for payment processing with Stripe")
@SecurityRequirement(name = "bearerAuth")
public class PaymentController {

    private final PaymentService paymentService;
    private final StripeService stripeService;
    private final StripeWebhookHandler webhookHandler;

    @PostMapping("/create-checkout-session")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create checkout session", description = "Create Stripe checkout session for policy purchase")
    public ApiResponse<CheckoutSessionResponse> createCheckoutSession(
            @Valid @RequestBody CheckoutSessionRequest request) {
        
        log.info("Creating checkout session for policy: {}", request.getPolicyTemplateId());
        
        // In real implementation, fetch policy details from Policy Service
        // For now, using placeholder values
        CheckoutSessionResponse response = paymentService.createCheckoutSession(
                request.getPolicyTemplateId(),
                "Insurance Policy", // Should come from Policy Service
                new java.math.BigDecimal("100.00") // Should come from Policy Service
        );
        
        return ApiResponse.success("Checkout session created successfully", response);
    }

    @PostMapping("/webhook")
    @Operation(summary = "Stripe webhook", description = "Handle Stripe webhook events")
    public ResponseEntity<String> handleStripeWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader) {
        
        log.info("Received Stripe webhook");

        try {
            // Verify webhook signature and construct event
            Event event = stripeService.constructEvent(payload, sigHeader);
            
            // Handle the event
            webhookHandler.handleWebhookEvent(event);
            
            return ResponseEntity.ok("Webhook processed successfully");

        } catch (Exception e) {
            log.error("Webhook processing failed", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Webhook processing failed: " + e.getMessage());
        }
    }

    @GetMapping("/config")
    @Operation(summary = "Get Stripe config", description = "Get Stripe publishable key for frontend")
    public ApiResponse<Map<String, String>> getStripeConfig() {
        log.debug("Fetching Stripe configuration");
        
        Map<String, String> config = new HashMap<>();
        config.put("publishableKey", paymentService.getPublishableKey());
        
        return ApiResponse.success(config);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get transaction", description = "Get transaction by ID")
    public ApiResponse<TransactionDto> getTransactionById(@PathVariable UUID id) {
        log.info("Fetching transaction: {}", id);
        TransactionDto transaction = paymentService.getTransactionById(id);
        return ApiResponse.success(transaction);
    }

    @GetMapping("/my-transactions")
    @Operation(summary = "Get my transactions", description = "Get current user's transactions")
    public ApiResponse<List<TransactionDto>> getMyTransactions() {
        log.info("Fetching current user's transactions");
        List<TransactionDto> transactions = paymentService.getMyTransactions();
        return ApiResponse.success(transactions);
    }

    @GetMapping("/my-transactions/history")
    @Operation(summary = "Get transaction history", description = "Get paginated transaction history")
    public ApiResponse<PageResponse<TransactionDto>> getMyTransactionHistory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        log.info("Fetching transaction history - page: {}, size: {}", page, size);
        PageResponse<TransactionDto> history = paymentService.getMyTransactionHistory(page, size);
        return ApiResponse.success(history);
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all transactions", description = "Get all transactions (Admin only)")
    public ApiResponse<PageResponse<TransactionDto>> getAllTransactions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        log.info("Admin fetching all transactions - page: {}, size: {}", page, size);
        PageResponse<TransactionDto> transactions = paymentService.getAllTransactions(page, size);
        return ApiResponse.success(transactions);
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get transactions by status", description = "Get transactions filtered by status (Admin only)")
    public ApiResponse<PageResponse<TransactionDto>> getTransactionsByStatus(
            @PathVariable String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        log.info("Admin fetching transactions by status: {} - page: {}, size: {}", status, page, size);
        PageResponse<TransactionDto> transactions = paymentService.getTransactionsByStatus(status, page, size);
        return ApiResponse.success(transactions);
    }

    @GetMapping("/statistics")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get statistics", description = "Get transaction statistics (Admin only)")
    public ApiResponse<TransactionStatistics> getStatistics() {
        log.info("Fetching transaction statistics");
        TransactionStatistics stats = paymentService.getTransactionStatistics();
        return ApiResponse.success(stats);
    }

    @PostMapping("/cleanup-stale")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Cleanup stale transactions", description = "Clean up stale pending transactions (Admin only)")
    public ApiResponse<Void> cleanupStaleTransactions() {
        log.info("Admin triggered stale transaction cleanup");
        paymentService.cleanupStaleTransactions();
        return ApiResponse.success("Stale transactions cleaned up successfully");
    }
}