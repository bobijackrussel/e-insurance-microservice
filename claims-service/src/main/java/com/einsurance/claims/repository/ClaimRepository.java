package com.einsurance.claims.repository;

import com.einsurance.claims.entity.Claim;
import com.einsurance.claims.entity.Claim.ClaimStatus;
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
 * Repository for Claim entity
 */
@Repository
public interface ClaimRepository extends JpaRepository<Claim, UUID> {

    /**
     * Find claim by claim number
     */
    Optional<Claim> findByClaimNumber(String claimNumber);

    /**
     * Find all claims for a user
     */
    List<Claim> findByUserId(UUID userId);

    /**
     * Find user claims with pagination
     */
    Page<Claim> findByUserId(UUID userId, Pageable pageable);

    /**
     * Find claims by customer policy
     */
    List<Claim> findByCustomerPolicyId(UUID customerPolicyId);

    /**
     * Find claims by status
     */
    List<Claim> findByStatus(ClaimStatus status);

    /**
     * Find claims by status with pagination
     */
    Page<Claim> findByStatus(ClaimStatus status, Pageable pageable);

    /**
     * Find pending claims for review
     */
    @Query("SELECT c FROM Claim c WHERE c.status = 'PENDING' " +
           "ORDER BY c.submittedDate ASC")
    List<Claim> findPendingClaims();

    /**
     * Find pending claims with pagination
     */
    @Query("SELECT c FROM Claim c WHERE c.status = 'PENDING' " +
           "ORDER BY c.submittedDate ASC")
    Page<Claim> findPendingClaims(Pageable pageable);

    /**
     * Find claims by user and status
     */
    List<Claim> findByUserIdAndStatus(UUID userId, ClaimStatus status);

    /**
     * Find claims reviewed by admin
     */
    List<Claim> findByReviewedBy(UUID reviewedBy);

    /**
     * Find claims submitted within date range
     */
    @Query("SELECT c FROM Claim c WHERE c.submittedDate BETWEEN :startDate AND :endDate " +
           "ORDER BY c.submittedDate DESC")
    List<Claim> findBySubmittedDateBetween(@Param("startDate") LocalDateTime startDate,
                                           @Param("endDate") LocalDateTime endDate);

    /**
     * Count claims by status
     */
    long countByStatus(ClaimStatus status);

    /**
     * Count user's claims
     */
    long countByUserId(UUID userId);

    /**
     * Sum of approved claim amounts
     */
    @Query("SELECT COALESCE(SUM(c.amount), 0) FROM Claim c " +
           "WHERE c.status IN ('APPROVED', 'PAID')")
    Double sumApprovedClaimAmounts();

    /**
     * Sum of claim amounts by user
     */
    @Query("SELECT COALESCE(SUM(c.amount), 0) FROM Claim c " +
           "WHERE c.userId = :userId AND c.status IN ('APPROVED', 'PAID')")
    Double sumApprovedClaimAmountsByUser(@Param("userId") UUID userId);

    /**
     * Check if user has pending claim for policy
     */
    @Query("SELECT COUNT(c) > 0 FROM Claim c " +
           "WHERE c.userId = :userId " +
           "AND c.customerPolicyId = :policyId " +
           "AND c.status IN ('PENDING', 'UNDER_REVIEW')")
    boolean hasPendingClaimForPolicy(@Param("userId") UUID userId, 
                                     @Param("policyId") UUID policyId);

    /**
     * Find claims requiring attention (pending > 7 days)
     */
    @Query("SELECT c FROM Claim c WHERE c.status = 'PENDING' " +
           "AND c.submittedDate < :cutoffDate " +
           "ORDER BY c.submittedDate ASC")
    List<Claim> findClaimsRequiringAttention(@Param("cutoffDate") LocalDateTime cutoffDate);
}