package com.einsurance.claims.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Claim entity representing insurance claim submissions
 * Customers submit claims for their active policies
 */
@Entity
@Table(name = "claims", indexes = {
    @Index(name = "idx_user_id", columnList = "user_id"),
    @Index(name = "idx_customer_policy_id", columnList = "customer_policy_id"),
    @Index(name = "idx_claim_number", columnList = "claim_number"),
    @Index(name = "idx_status", columnList = "status")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Claim {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(columnDefinition = "UUID")
    private UUID id;

    @Column(name = "claim_number", unique = true, nullable = false, length = 50)
    private String claimNumber;

    @Column(name = "user_id", nullable = false, columnDefinition = "UUID")
    private UUID userId;

    @Column(name = "customer_policy_id", nullable = false, columnDefinition = "UUID")
    private UUID customerPolicyId;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "incident_date", nullable = false)
    private LocalDate incidentDate;

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private ClaimStatus status = ClaimStatus.PENDING;

    @Column(name = "submitted_date", nullable = false)
    @CreationTimestamp
    private LocalDateTime submittedDate;

    @Column(name = "reviewed_date")
    private LocalDateTime reviewedDate;

    @Column(name = "reviewed_by", columnDefinition = "UUID")
    private UUID reviewedBy;

    @Column(name = "admin_notes", columnDefinition = "TEXT")
    private String adminNotes;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Claim status enumeration
     */
    public enum ClaimStatus {
        PENDING,
        UNDER_REVIEW,
        APPROVED,
        REJECTED,
        PAID
    }

    /**
     * Check if claim is pending
     */
    public boolean isPending() {
        return status == ClaimStatus.PENDING;
    }

    /**
     * Check if claim is approved
     */
    public boolean isApproved() {
        return status == ClaimStatus.APPROVED || status == ClaimStatus.PAID;
    }

    /**
     * Check if claim can be reviewed
     */
    public boolean canBeReviewed() {
        return status == ClaimStatus.PENDING || status == ClaimStatus.UNDER_REVIEW;
    }

    /**
     * Mark as under review
     */
    public void markAsUnderReview(UUID reviewerId) {
        this.status = ClaimStatus.UNDER_REVIEW;
        this.reviewedBy = reviewerId;
        this.reviewedDate = LocalDateTime.now();
    }

    /**
     * Approve claim
     */
    public void approve(UUID reviewerId, String notes) {
        this.status = ClaimStatus.APPROVED;
        this.reviewedBy = reviewerId;
        this.reviewedDate = LocalDateTime.now();
        this.adminNotes = notes;
    }

    /**
     * Reject claim
     */
    public void reject(UUID reviewerId, String notes) {
        this.status = ClaimStatus.REJECTED;
        this.reviewedBy = reviewerId;
        this.reviewedDate = LocalDateTime.now();
        this.adminNotes = notes;
    }

    /**
     * Mark as paid
     */
    public void markAsPaid() {
        if (status != ClaimStatus.APPROVED) {
            throw new IllegalStateException("Only approved claims can be marked as paid");
        }
        this.status = ClaimStatus.PAID;
    }

    @PrePersist
    public void prePersist() {
        if (status == null) {
            status = ClaimStatus.PENDING;
        }
    }
}