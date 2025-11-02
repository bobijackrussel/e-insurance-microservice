package com.einsurance.payment.service;

import com.einsurance.common.dto.CheckoutSessionResponse;
import com.einsurance.common.exception.ResourceNotFoundException;
import com.einsurance.payment.entity.Transaction;
import com.einsurance.payment.entity.Transaction.TransactionStatus;
import com.einsurance.payment.mapper.TransactionMapper;
import com.einsurance.payment.repository.TransactionRepository;
import com.stripe.model.checkout.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for PaymentService
 */
@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private TransactionMapper transactionMapper;

    @Mock
    private StripeService stripeService;

    @Mock
    private WebClient.Builder webClientBuilder;

    @InjectMocks
    private PaymentService paymentService;

    private Transaction testTransaction;
    private UUID userId;
    private UUID policyId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        policyId = UUID.randomUUID();

        testTransaction = Transaction.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .amount(new BigDecimal("100.00"))
                .currency("EUR")
                .status(TransactionStatus.PENDING)
                .stripeSessionId("cs_test_123")
                .build();
    }

    @Test
    void handlePaymentSuccess_Success() {
        // Given
        UUID customerPolicyId = UUID.randomUUID();
        String sessionId = "cs_test_123";
        
        Session mockSession = mock(Session.class);
        when(mockSession.getPaymentIntent()).thenReturn("pi_test_123");
        when(mockSession.getPaymentMethodTypes()).thenReturn(java.util.List.of("card"));
        
        when(transactionRepository.findByStripeSessionId(sessionId))
                .thenReturn(Optional.of(testTransaction));
        when(stripeService.retrieveSession(sessionId)).thenReturn(mockSession);
        when(transactionRepository.save(any(Transaction.class))).thenReturn(testTransaction);

        // When
        paymentService.handlePaymentSuccess(sessionId, customerPolicyId);

        // Then
        verify(transactionRepository, times(1)).save(any(Transaction.class));
        assertEquals(TransactionStatus.COMPLETED, testTransaction.getStatus());
        assertEquals(customerPolicyId, testTransaction.getCustomerPolicyId());
    }

    @Test
    void handlePaymentSuccess_ThrowsException_WhenTransactionNotFound() {
        // Given
        String sessionId = "cs_test_nonexistent";
        UUID customerPolicyId = UUID.randomUUID();
        
        when(transactionRepository.findByStripeSessionId(sessionId))
                .thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class,
                () -> paymentService.handlePaymentSuccess(sessionId, customerPolicyId));
        
        verify(transactionRepository, never()).save(any(Transaction.class));
    }

    @Test
    void handlePaymentFailure_Success() {
        // Given
        String sessionId = "cs_test_123";
        String reason = "Payment declined";
        
        when(transactionRepository.findByStripeSessionId(sessionId))
                .thenReturn(Optional.of(testTransaction));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(testTransaction);

        // When
        paymentService.handlePaymentFailure(sessionId, reason);

        // Then
        verify(transactionRepository, times(1)).save(any(Transaction.class));
        assertEquals(TransactionStatus.FAILED, testTransaction.getStatus());
        assertEquals(reason, testTransaction.getFailureReason());
    }

    @Test
    void getTransactionStatistics_Success() {
        // Given
        when(transactionRepository.count()).thenReturn(100L);
        when(transactionRepository.countByStatus(TransactionStatus.COMPLETED)).thenReturn(80L);
        when(transactionRepository.countByStatus(TransactionStatus.FAILED)).thenReturn(15L);
        when(transactionRepository.countByStatus(TransactionStatus.PENDING)).thenReturn(5L);
        when(transactionRepository.sumCompletedTransactions()).thenReturn(8000.0);

        // When
        TransactionStatistics stats = paymentService.getTransactionStatistics();

        // Then
        assertNotNull(stats);
        assertEquals(100L, stats.getTotalTransactions());
        assertEquals(80L, stats.getCompletedTransactions());
        assertEquals(15L, stats.getFailedTransactions());
        assertEquals(5L, stats.getPendingTransactions());
        assertEquals(8000.0, stats.getTotalRevenue());
    }
}