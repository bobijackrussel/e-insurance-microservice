package com.einsurance.payment.service;

import com.einsurance.common.dto.CheckoutSessionResponse;
import com.einsurance.common.dto.PageResponse;
import com.einsurance.common.dto.TransactionDto;
import com.einsurance.common.exception.PaymentException;
import com.einsurance.common.exception.ResourceNotFoundException;
import com.einsurance.common.security.SecurityUtil;
import com.einsurance.payment.entity.Transaction;
import com.einsurance.payment.entity.Transaction.TransactionStatus;
import com.einsurance.payment.mapper.TransactionMapper;
import com.einsurance.payment.repository.TransactionRepository;
import com.stripe.model.checkout.Session;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Payment service
 * Manages payment transactions and Stripe integration
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final TransactionRepository transactionRepository;
    private final TransactionMapper transactionMapper;
    private final StripeService stripeService;
    private final WebClient.Builder webClientBuilder;

    /**
     * Create checkout session for policy purchase
     */
    @Transactional
    public CheckoutSessionResponse createCheckoutSession(
            UUID policyTemplateId, 
            String policyName,
            BigDecimal amount) {
        
        String currentUserId = SecurityUtil.getCurrentUserIdOrThrow();
        UUID userId = UUID.fromString(currentUserId);

        log.info("Creating checkout session for user: {}, policy: {}", userId, policyTemplateId);

        // Create transaction record
        Transaction transaction = Transaction.builder()
                .userId(userId)
                .amount(amount)
                .currency("EUR")
                .status(TransactionStatus.PENDING)
                .build();

        Transaction savedTransaction = transactionRepository.save(transaction);

        // Create Stripe Checkout Session
        Session stripeSession = stripeService.createCheckoutSession(
                userId,
                policyTemplateId,
                amount,
                policyName,
                "EUR"
        );

        // Update transaction with Stripe session ID
        savedTransaction.setStripeSessionId(stripeSession.getId());
        savedTransaction.setStripePaymentIntentId(stripeSession.getPaymentIntent());
        transactionRepository.save(savedTransaction);

        log.info("Checkout session created: transaction={}, stripeSession={}", 
                savedTransaction.getId(), stripeSession.getId());

        return CheckoutSessionResponse.builder()
                .sessionId(stripeSession.getId())
                .sessionUrl(stripeSession.getUrl())
                .transactionId(savedTransaction.getId())
                .build();
    }

    /**
     * Handle successful payment (called by webhook)
     */
    @Transactional
    public void handlePaymentSuccess(String sessionId, UUID customerPolicyId) {
        log.info("Handling payment success for session: {}", sessionId);

        Transaction transaction = transactionRepository.findByStripeSessionId(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction", "stripeSessionId", sessionId));

        // Update transaction status
        transaction.markAsCompleted();
        transaction.setCustomerPolicyId(customerPolicyId);

        // Retrieve session details from Stripe
        Session stripeSession = stripeService.retrieveSession(sessionId);
        transaction.setStripePaymentIntentId(stripeSession.getPaymentIntent());
        transaction.setStripeChargeId(stripeSession.getPaymentIntent()); // Simplified
        transaction.setPaymentMethod(stripeSession.getPaymentMethodTypes().get(0));

        transactionRepository.save(transaction);

        log.info("Payment processed successfully: transaction={}, policy={}", 
                transaction.getId(), customerPolicyId);

        // Confirm policy purchase with Policy Service
        confirmPolicyPurchase(customerPolicyId, transaction.getId());
    }

    /**
     * Handle payment failure (called by webhook)
     */
    @Transactional
    public void handlePaymentFailure(String sessionId, String reason) {
        log.warn("Handling payment failure for session: {}, reason: {}", sessionId, reason);

        Transaction transaction = transactionRepository.findByStripeSessionId(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction", "stripeSessionId", sessionId));

        transaction.markAsFailed(reason);
        transactionRepository.save(transaction);

        log.info("Payment marked as failed: transaction={}", transaction.getId());
    }

    /**
     * Get transaction by ID
     */
    @Transactional(readOnly = true)
    public TransactionDto getTransactionById(UUID id) {
        log.debug("Fetching transaction by ID: {}", id);

        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction", "id", id));

        // Check authorization
        String currentUserId = SecurityUtil.getCurrentUserIdOrThrow();
        if (!transaction.getUserId().toString().equals(currentUserId) && !SecurityUtil.isAdmin()) {
            throw new PaymentException("You don't have permission to view this transaction");
        }

        return transactionMapper.toDto(transaction);
    }

    /**
     * Get current user's transactions
     */
    @Transactional(readOnly = true)
    public List<TransactionDto> getMyTransactions() {
        String currentUserId = SecurityUtil.getCurrentUserIdOrThrow();
        UUID userId = UUID.fromString(currentUserId);

        log.debug("Fetching transactions for user: {}", userId);

        List<Transaction> transactions = transactionRepository.findByUserId(userId);
        return transactionMapper.toDtoList(transactions);
    }

    /**
     * Get user's transaction history with pagination
     */
    @Transactional(readOnly = true)
    public PageResponse<TransactionDto> getMyTransactionHistory(int page, int size) {
        String currentUserId = SecurityUtil.getCurrentUserIdOrThrow();
        UUID userId = UUID.fromString(currentUserId);

        log.debug("Fetching transaction history for user: {} - page: {}, size: {}", userId, page, size);

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Transaction> transactionPage = transactionRepository.findByUserId(userId, pageable);

        return buildPageResponse(transactionPage);
    }

    /**
     * Get all transactions (Admin only)
     */
    @Transactional(readOnly = true)
    public PageResponse<TransactionDto> getAllTransactions(int page, int size) {
        log.debug("Admin fetching all transactions - page: {}, size: {}", page, size);

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Transaction> transactionPage = transactionRepository.findAll(pageable);

        return buildPageResponse(transactionPage);
    }

    /**
     * Get transactions by status (Admin only)
     */
    @Transactional(readOnly = true)
    public PageResponse<TransactionDto> getTransactionsByStatus(String status, int page, int size) {
        log.debug("Admin fetching transactions by status: {} - page: {}, size: {}", status, page, size);

        TransactionStatus transactionStatus = TransactionStatus.valueOf(status.toUpperCase());
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Transaction> transactionPage = transactionRepository.findByStatus(transactionStatus, pageable);

        return buildPageResponse(transactionPage);
    }

    /**
     * Get transaction statistics (Admin only)
     */
    @Transactional(readOnly = true)
    public TransactionStatistics getTransactionStatistics() {
        log.debug("Fetching transaction statistics");

        long totalTransactions = transactionRepository.count();
        long completedTransactions = transactionRepository.countByStatus(TransactionStatus.COMPLETED);
        long failedTransactions = transactionRepository.countByStatus(TransactionStatus.FAILED);
        long pendingTransactions = transactionRepository.countByStatus(TransactionStatus.PENDING);
        Double totalRevenue = transactionRepository.sumCompletedTransactions();

        return TransactionStatistics.builder()
                .totalTransactions(totalTransactions)
                .completedTransactions(completedTransactions)
                .failedTransactions(failedTransactions)
                .pendingTransactions(pendingTransactions)
                .totalRevenue(totalRevenue != null ? totalRevenue : 0.0)
                .build();
    }

    /**
     * Confirm policy purchase with Policy Service
     */
    private void confirmPolicyPurchase(UUID policyId, UUID transactionId) {
        log.info("Confirming policy purchase: {} with transaction: {}", policyId, transactionId);

        try {
            WebClient webClient = webClientBuilder.baseUrl("lb://policy-service").build();

            webClient.post()
                    .uri("/api/policies/{policyId}/confirm", policyId)
                    .bodyValue(Map.of("transactionId", transactionId))
                    .retrieve()
                    .bodyToMono(Void.class)
                    .block();

            log.info("Policy purchase confirmed successfully");
        } catch (Exception e) {
            log.error("Failed to confirm policy purchase", e);
            // Don't throw - transaction is already saved
        }
    }

    /**
     * Get Stripe publishable key
     */
    public String getPublishableKey() {
        return stripeService.getPublishableKey();
    }

    /**
     * Cleanup stale pending transactions (Admin only)
     * Marks transactions pending for more than 24 hours as failed
     */
    @Transactional
    public void cleanupStaleTransactions() {
        log.info("Cleaning up stale pending transactions");

        LocalDateTime cutoffTime = LocalDateTime.now().minusHours(24);
        List<Transaction> staleTransactions = transactionRepository.findStaleTransactions(cutoffTime);

        for (Transaction transaction : staleTransactions) {
            transaction.markAsFailed("Transaction expired");
            transactionRepository.save(transaction);
            log.debug("Marked transaction as failed: {}", transaction.getId());
        }

        log.info("Cleaned up {} stale transactions", staleTransactions.size());
    }

    /**
     * Helper method to build PageResponse
     */
    private PageResponse<TransactionDto> buildPageResponse(Page<Transaction> transactionPage) {
        List<TransactionDto> dtos = transactionMapper.toDtoList(transactionPage.getContent());

        return PageResponse.<TransactionDto>builder()
                .content(dtos)
                .page(transactionPage.getNumber())
                .size(transactionPage.getSize())
                .totalElements(transactionPage.getTotalElements())
                .totalPages(transactionPage.getTotalPages())
                .isLast(transactionPage.isLast())
                .build();
    }
}