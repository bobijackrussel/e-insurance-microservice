package com.einsurance.payment.service;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Statistics DTO for payment transactions
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionStatistics {
    private long totalTransactions;
    private long completedTransactions;
    private long failedTransactions;
    private long pendingTransactions;
    private Double totalRevenue;
}
