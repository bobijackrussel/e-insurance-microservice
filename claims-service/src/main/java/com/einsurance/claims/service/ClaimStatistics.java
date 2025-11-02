package com.einsurance.claims.service;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Statistics DTO for claims
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClaimStatistics {
    private long totalClaims;
    private long pendingClaims;
    private long underReviewClaims;
    private long approvedClaims;
    private long rejectedClaims;
    private long paidClaims;
    private Double totalApprovedAmount;
}
