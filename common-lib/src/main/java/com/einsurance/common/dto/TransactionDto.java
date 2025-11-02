package com.einsurance.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * DTO representing a payment transaction.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TransactionDto {
    private UUID id;
    private UUID userId;
    private UUID customerPolicyId;
    private BigDecimal amount;
    private String currency;
    private String paymentMethod;
    private String stripePaymentIntentId;
    private String stripeSessionId;
    private String stripeChargeId;
    private String status;
    private String failureReason;
    private Map<String, Object> metadata;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime completedAt;
}
