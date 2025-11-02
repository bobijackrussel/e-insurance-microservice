package com.einsurance.policy.service;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Statistics DTO for customer policies
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerPolicyStatistics {
    private long totalPolicies;
    private long activePolicies;
    private long expiredPolicies;
    private long cancelledPolicies;
    private long suspendedPolicies;
}
