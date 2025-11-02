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
 * DTO representing an insurance claim.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ClaimDto {
    private UUID id;
    private String claimNumber;
    private UUID userId;
    private UUID customerPolicyId;
    private BigDecimal amount;
    private String description;
    private LocalDate incidentDate;
    private String status;
    private LocalDateTime submittedDate;
    private LocalDateTime reviewedDate;
    private UUID reviewedBy;
    private String adminNotes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
