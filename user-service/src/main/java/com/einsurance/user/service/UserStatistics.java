package com.einsurance.user.service;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for user statistics
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserStatistics {
    private Long totalUsers;
    private Long activeUsers;
    private Long adminUsers;
    private Long customerUsers;
}