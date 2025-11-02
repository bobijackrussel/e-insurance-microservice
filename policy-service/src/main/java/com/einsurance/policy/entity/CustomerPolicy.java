package com.einsurance.policy.entity;

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
 * Customer Policy entity representing purchased insurance policies
 * Links users to their purchased policy templates
 */
@Entity
@Table(name = "customer_policies", indexes = {
    @Index(name = "idx_user_id", columnList = "user_id"),
    @Index(name = "idx_policy_number", columnList = "policy_number"),
    @Index(name = "idx_status", columnList = "status"),
    @Index(name = "idx_expiry_date", columnList = "expiry_date")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerPolicy {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(columnDefinition = "UUID")
    private UUID id;

    @Column(name = "policy_number", unique = true, nullable = false, length = 50)
    private String policyNumber;

    @Column(name = "user_id", nullable = false, columnDefinition = "UUID")
    private UUID userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "policy_template_id", nullable = false)
    private PolicyTemplate policyTemplate;

    @Column(name = "purchase_date", nullable = false)
    @CreationTimestamp
    private LocalDateTime purchaseDate;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "expiry_date", nullable = false)
    private LocalDate expiryDate;

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private PolicyStatus status = PolicyStatus.PENDING;

    @Column(name = "payment_transaction_id", columnDefinition = "UUID")
    private UUID paymentTransactionId;

    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Policy status enumeration
     */
    public enum PolicyStatus {
        PENDING,    // Awaiting payment confirmation
        ACTIVE,
        EXPIRED,
        CANCELLED,
        SUSPENDED
    }

    /**
     * Check if policy is active
     */
    public boolean isActive() {
        return status == PolicyStatus.ACTIVE && !isExpired();
    }

    /**
     * Check if policy is expired
     */
    public boolean isExpired() {
        return expiryDate != null && expiryDate.isBefore(LocalDate.now());
    }

    /**
     * Get remaining days
     */
    public long getRemainingDays() {
        if (expiryDate == null) {
            return 0;
        }
        return LocalDate.now().until(expiryDate, java.time.temporal.ChronoUnit.DAYS);
    }

    /**
     * Check if policy can be used for claims
     */
    public boolean canFileclaim() {
        return isActive() && status == PolicyStatus.ACTIVE;
    }

    @PrePersist
    public void prePersist() {
        if (status == null) {
            status = PolicyStatus.PENDING;
        }
        if (startDate == null) {
            startDate = LocalDate.now();
        }
    }

    @PreUpdate
    public void preUpdate() {
        // Auto-update status if expired
        if (isExpired() && status == PolicyStatus.ACTIVE) {
            status = PolicyStatus.EXPIRED;
        }
    }
}