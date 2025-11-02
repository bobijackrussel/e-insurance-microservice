package com.einsurance.payment.repository;

import com.einsurance.payment.entity.Transaction;
import com.einsurance.payment.entity.Transaction.TransactionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for Transaction entity
 */
@Repository
public interface TransactionRepository extends JpaRepository<Transaction, UUID> {

    /**
     * Find transaction by Stripe payment intent ID
     */
    Optional<Transaction> findByStripePaymentIntentId(String stripePaymentIntentId);

    /**
     * Find transaction by Stripe session ID
     */
    Optional<Transaction> findByStripeSessionId(String stripeSessionId);

    /**
     * Find all transactions for a user
     */
    List<Transaction> findByUserId(UUID userId);

    /**
     * Find user transactions with pagination
     */
    Page<Transaction> findByUserId(UUID userId, Pageable pageable);

    /**
     * Find transactions by status
     */
    List<Transaction> findByStatus(TransactionStatus status);

    /**
     * Find transactions by status with pagination
     */
    Page<Transaction> findByStatus(TransactionStatus status, Pageable pageable);

    /**
     * Find completed transactions for user
     */
    @Query("SELECT t FROM Transaction t WHERE t.userId = :userId " +
           "AND t.status = 'COMPLETED' ORDER BY t.completedAt DESC")
    List<Transaction> findCompletedByUserId(@Param("userId") UUID userId);

    /**
     * Find transactions by date range
     */
    @Query("SELECT t FROM Transaction t WHERE t.createdAt BETWEEN :startDate AND :endDate " +
           "ORDER BY t.createdAt DESC")
    List<Transaction> findByDateRange(@Param("startDate") LocalDateTime startDate,
                                      @Param("endDate") LocalDateTime endDate);

    /**
     * Find pending transactions older than specified time
     */
    @Query("SELECT t FROM Transaction t WHERE t.status IN ('PENDING', 'PROCESSING') " +
           "AND t.createdAt < :cutoffTime")
    List<Transaction> findStaleTransactions(@Param("cutoffTime") LocalDateTime cutoffTime);

    /**
     * Count transactions by status
     */
    long countByStatus(TransactionStatus status);

    /**
     * Count user's transactions
     */
    long countByUserId(UUID userId);

    /**
     * Sum of completed transaction amounts
     */
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t " +
           "WHERE t.status = 'COMPLETED'")
    Double sumCompletedTransactions();

    /**
     * Sum of completed transaction amounts for user
     */
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t " +
           "WHERE t.userId = :userId AND t.status = 'COMPLETED'")
    Double sumCompletedTransactionsByUser(@Param("userId") UUID userId);

    /**
     * Check if transaction exists by Stripe session ID
     */
    boolean existsByStripeSessionId(String stripeSessionId);
}