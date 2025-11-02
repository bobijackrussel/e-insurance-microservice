package com.einsurance.claims.service;

import com.einsurance.claims.entity.Claim;
import com.einsurance.claims.entity.Claim.ClaimStatus;
import com.einsurance.claims.mapper.ClaimMapper;
import com.einsurance.claims.repository.ClaimRepository;
import com.einsurance.common.dto.ClaimDto;
import com.einsurance.common.dto.ClaimReviewRequest;
import com.einsurance.common.dto.ClaimSubmissionRequest;
import com.einsurance.common.exception.ClaimException;
import com.einsurance.common.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ClaimsService
 */
@ExtendWith(MockitoExtension.class)
class ClaimsServiceTest {

    @Mock
    private ClaimRepository claimRepository;

    @Mock
    private ClaimMapper claimMapper;

    @Mock
    private WebClient.Builder webClientBuilder;

    @InjectMocks
    private ClaimsService claimsService;

    private Claim testClaim;
    private ClaimDto testClaimDto;
    private UUID userId;
    private UUID policyId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        policyId = UUID.randomUUID();

        testClaim = Claim.builder()
                .id(UUID.randomUUID())
                .claimNumber("CLM-2025-ABC123")
                .userId(userId)
                .customerPolicyId(policyId)
                .amount(new BigDecimal("5000.00"))
                .description("Car accident damage")
                .incidentDate(LocalDate.now().minusDays(7))
                .status(ClaimStatus.PENDING)
                .build();

        testClaimDto = ClaimDto.builder()
                .id(testClaim.getId())
                .claimNumber(testClaim.getClaimNumber())
                .userId(testClaim.getUserId())
                .customerPolicyId(testClaim.getCustomerPolicyId())
                .amount(testClaim.getAmount())
                .description(testClaim.getDescription())
                .incidentDate(testClaim.getIncidentDate())
                .status(testClaim.getStatus().name())
                .build();
    }

    @Test
    void getClaimById_Success() {
        // Given
        UUID claimId = testClaim.getId();
        when(claimRepository.findById(claimId)).thenReturn(Optional.of(testClaim));
        when(claimMapper.toDto(any(Claim.class))).thenReturn(testClaimDto);

        // When
        ClaimDto result = claimsService.getClaimById(claimId);

        // Then
        assertNotNull(result);
        assertEquals(testClaimDto.getClaimNumber(), result.getClaimNumber());
    }

    @Test
    void getClaimById_ThrowsException_WhenNotFound() {
        // Given
        UUID claimId = UUID.randomUUID();
        when(claimRepository.findById(claimId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class,
                () -> claimsService.getClaimById(claimId));
    }

    @Test
    void markAsUnderReview_Success() {
        // Given
        UUID claimId = testClaim.getId();
        when(claimRepository.findById(claimId)).thenReturn(Optional.of(testClaim));
        when(claimRepository.save(any(Claim.class))).thenReturn(testClaim);

        // When
        claimsService.markAsUnderReview(claimId);

        // Then
        verify(claimRepository, times(1)).save(any(Claim.class));
        assertEquals(ClaimStatus.UNDER_REVIEW, testClaim.getStatus());
    }

    @Test
    void markAsUnderReview_ThrowsException_WhenNotPending() {
        // Given
        testClaim.setStatus(ClaimStatus.APPROVED);
        UUID claimId = testClaim.getId();
        when(claimRepository.findById(claimId)).thenReturn(Optional.of(testClaim));

        // When & Then
        assertThrows(ClaimException.class,
                () -> claimsService.markAsUnderReview(claimId));
        verify(claimRepository, never()).save(any(Claim.class));
    }

    @Test
    void markAsPaid_Success() {
        // Given
        testClaim.setStatus(ClaimStatus.APPROVED);
        UUID claimId = testClaim.getId();
        when(claimRepository.findById(claimId)).thenReturn(Optional.of(testClaim));
        when(claimRepository.save(any(Claim.class))).thenReturn(testClaim);

        // When
        claimsService.markAsPaid(claimId);

        // Then
        verify(claimRepository, times(1)).save(any(Claim.class));
        assertEquals(ClaimStatus.PAID, testClaim.getStatus());
    }

    @Test
    void markAsPaid_ThrowsException_WhenNotApproved() {
        // Given
        testClaim.setStatus(ClaimStatus.PENDING);
        UUID claimId = testClaim.getId();
        when(claimRepository.findById(claimId)).thenReturn(Optional.of(testClaim));

        // When & Then
        assertThrows(IllegalStateException.class,
                () -> claimsService.markAsPaid(claimId));
        verify(claimRepository, never()).save(any(Claim.class));
    }

    @Test
    void getClaimStatistics_Success() {
        // Given
        when(claimRepository.count()).thenReturn(100L);
        when(claimRepository.countByStatus(ClaimStatus.PENDING)).thenReturn(20L);
        when(claimRepository.countByStatus(ClaimStatus.UNDER_REVIEW)).thenReturn(15L);
        when(claimRepository.countByStatus(ClaimStatus.APPROVED)).thenReturn(40L);
        when(claimRepository.countByStatus(ClaimStatus.REJECTED)).thenReturn(20L);
        when(claimRepository.countByStatus(ClaimStatus.PAID)).thenReturn(5L);
        when(claimRepository.sumApprovedClaimAmounts()).thenReturn(45000.0);

        // When
        ClaimStatistics stats = claimsService.getClaimStatistics();

        // Then
        assertNotNull(stats);
        assertEquals(100L, stats.getTotalClaims());
        assertEquals(20L, stats.getPendingClaims());
        assertEquals(15L, stats.getUnderReviewClaims());
        assertEquals(40L, stats.getApprovedClaims());
        assertEquals(20L, stats.getRejectedClaims());
        assertEquals(5L, stats.getPaidClaims());
        assertEquals(45000.0, stats.getTotalApprovedAmount());
    }
}