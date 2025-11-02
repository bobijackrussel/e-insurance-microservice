package com.einsurance.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO representing a customer's purchased policy.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CustomerPolicyDto {
    private UUID id;
    private String policyNumber;
    private UUID userId;
    private UUID policyTemplateId;
    private String policyTemplateName;
    private String policyType;
    private LocalDateTime purchaseDate;
    private LocalDate startDate;
    private LocalDate expiryDate;
    private String status;
    private UUID paymentTransactionId;
    private BigDecimal totalAmount;
    private Long remainingDays;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Nested policy template details (for notifications and PDF generation)
    private PolicyTemplateDto policyTemplate;
}
