package com.einsurance.claims.service;

import com.einsurance.claims.entity.Claim;
import com.einsurance.claims.entity.Claim.ClaimStatus;
import com.einsurance.claims.mapper.ClaimMapper;
import com.einsurance.claims.repository.ClaimRepository;
import com.einsurance.common.dto.ClaimDto;
import com.einsurance.common.dto.ClaimReviewRequest;
import com.einsurance.common.dto.ClaimSubmissionRequest;
import com.einsurance.common.dto.PageResponse;
import com.einsurance.common.exception.ClaimException;
import com.einsurance.common.exception.ResourceNotFoundException;
import com.einsurance.common.exception.ValidationException;
import com.einsurance.common.security.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Service for Claims operations
 * Manages claim submissions and approval workflow
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ClaimsService {

    private final ClaimRepository claimRepository;
    private final ClaimMapper claimMapper;
    private final WebClient.Builder webClientBuilder;

    /**
     * Submit a new claim
     */
    @Transactional
    public ClaimDto submitClaim(ClaimSubmissionRequest request) {
        String currentUserId = SecurityUtil.getCurrentUserIdOrThrow();
        UUID userId = UUID.fromString(currentUserId);

        log.info("User {} submitting claim for policy: {}", userId, request.getCustomerPolicyId());

        // Validate incident date
        if (request.getIncidentDate().isAfter(LocalDate.now())) {
            throw new ValidationException("incidentDate", "Incident date cannot be in the future");
        }

        // Validate policy ownership and status
        validatePolicyForClaim(userId, request.getCustomerPolicyId());

        // Check if user already has pending claim for this policy
        if (claimRepository.hasPendingClaimForPolicy(userId, request.getCustomerPolicyId())) {
            throw new ClaimException("You already have a pending claim for this policy");
        }

        // Create claim entity
        Claim claim = claimMapper.toEntity(request);
        claim.setUserId(userId);
        claim.setStatus(ClaimStatus.PENDING);

        // Generate claim number
        claim.setClaimNumber(generateClaimNumber());

        Claim savedClaim = claimRepository.save(claim);
        log.info("Claim submitted successfully: {}", savedClaim.getClaimNumber());

        // Notify user via email (async)
        notifyClaimSubmitted(savedClaim);

        return claimMapper.toDto(savedClaim);
    }

    /**
     * Get claim by ID
     */
    @Transactional(readOnly = true)
    public ClaimDto getClaimById(UUID id) {
        log.debug("Fetching claim by ID: {}", id);

        Claim claim = claimRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Claim", "id", id));

        // Check authorization
        String currentUserId = SecurityUtil.getCurrentUserIdOrThrow();
        if (!claim.getUserId().toString().equals(currentUserId) && !SecurityUtil.isAdmin()) {
            throw new ClaimException("You don't have permission to view this claim");
        }

        return claimMapper.toDto(claim);
    }

    /**
     * Get claim by claim number
     */
    @Transactional(readOnly = true)
    public ClaimDto getClaimByClaimNumber(String claimNumber) {
        log.debug("Fetching claim by claim number: {}", claimNumber);

        Claim claim = claimRepository.findByClaimNumber(claimNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Claim", "claimNumber", claimNumber));

        // Check authorization
        String currentUserId = SecurityUtil.getCurrentUserIdOrThrow();
        if (!claim.getUserId().toString().equals(currentUserId) && !SecurityUtil.isAdmin()) {
            throw new ClaimException("You don't have permission to view this claim");
        }

        return claimMapper.toDto(claim);
    }

    /**
     * Get current user's claims
     */
    @Transactional(readOnly = true)
    public List<ClaimDto> getMyClaims() {
        String currentUserId = SecurityUtil.getCurrentUserIdOrThrow();
        UUID userId = UUID.fromString(currentUserId);

        log.debug("Fetching claims for user: {}", userId);

        List<Claim> claims = claimRepository.findByUserId(userId);
        return claimMapper.toDtoList(claims);
    }

    /**
     * Get current user's claims with pagination
     */
    @Transactional(readOnly = true)
    public PageResponse<ClaimDto> getMyClaimsHistory(int page, int size) {
        String currentUserId = SecurityUtil.getCurrentUserIdOrThrow();
        UUID userId = UUID.fromString(currentUserId);

        log.debug("Fetching claim history for user: {} - page: {}, size: {}", userId, page, size);

        Pageable pageable = PageRequest.of(page, size, Sort.by("submittedDate").descending());
        Page<Claim> claimPage = claimRepository.findByUserId(userId, pageable);

        return buildPageResponse(claimPage);
    }

    /**
     * Get all claims (Admin only)
     */
    @Transactional(readOnly = true)
    public PageResponse<ClaimDto> getAllClaims(int page, int size) {
        log.debug("Admin fetching all claims - page: {}, size: {}", page, size);

        Pageable pageable = PageRequest.of(page, size, Sort.by("submittedDate").descending());
        Page<Claim> claimPage = claimRepository.findAll(pageable);

        return buildPageResponse(claimPage);
    }

    /**
     * Get claims by status (Admin only)
     */
    @Transactional(readOnly = true)
    public PageResponse<ClaimDto> getClaimsByStatus(String status, int page, int size) {
        log.debug("Admin fetching claims by status: {} - page: {}, size: {}", status, page, size);

        ClaimStatus claimStatus = ClaimStatus.valueOf(status.toUpperCase());
        Pageable pageable = PageRequest.of(page, size, Sort.by("submittedDate").descending());
        Page<Claim> claimPage = claimRepository.findByStatus(claimStatus, pageable);

        return buildPageResponse(claimPage);
    }

    /**
     * Get pending claims for review (Admin only)
     */
    @Transactional(readOnly = true)
    public PageResponse<ClaimDto> getPendingClaims(int page, int size) {
        log.debug("Admin fetching pending claims - page: {}, size: {}", page, size);

        Pageable pageable = PageRequest.of(page, size);
        Page<Claim> claimPage = claimRepository.findPendingClaims(pageable);

        return buildPageResponse(claimPage);
    }

    /**
     * Review claim - approve or reject (Admin only)
     */
    @Transactional
    public ClaimDto reviewClaim(UUID claimId, ClaimReviewRequest request) {
        String currentAdminId = SecurityUtil.getCurrentUserIdOrThrow();
        UUID adminId = UUID.fromString(currentAdminId);

        log.info("Admin {} reviewing claim: {}", adminId, claimId);

        Claim claim = claimRepository.findById(claimId)
                .orElseThrow(() -> new ResourceNotFoundException("Claim", "id", claimId));

        // Validate claim can be reviewed
        if (!claim.canBeReviewed()) {
            throw new ClaimException("Claim cannot be reviewed in current status: " + claim.getStatus());
        }

        // Approve or reject based on request
        if ("APPROVED".equalsIgnoreCase(request.getStatus())) {
            claim.approve(adminId, request.getAdminNotes());
            log.info("Claim approved: {}", claim.getClaimNumber());
        } else if ("REJECTED".equalsIgnoreCase(request.getStatus())) {
            claim.reject(adminId, request.getAdminNotes());
            log.info("Claim rejected: {}", claim.getClaimNumber());
        } else {
            throw new ValidationException("status", "Status must be APPROVED or REJECTED");
        }

        Claim reviewedClaim = claimRepository.save(claim);

        // Notify user via email
        notifyClaimReviewed(reviewedClaim);

        return claimMapper.toDto(reviewedClaim);
    }

    /**
     * Mark claim as under review (Admin only)
     */
    @Transactional
    public void markAsUnderReview(UUID claimId) {
        String currentAdminId = SecurityUtil.getCurrentUserIdOrThrow();
        UUID adminId = UUID.fromString(currentAdminId);

        log.info("Admin {} marking claim as under review: {}", adminId, claimId);

        Claim claim = claimRepository.findById(claimId)
                .orElseThrow(() -> new ResourceNotFoundException("Claim", "id", claimId));

        if (!claim.isPending()) {
            throw new ClaimException("Only pending claims can be marked as under review");
        }

        claim.markAsUnderReview(adminId);
        claimRepository.save(claim);

        log.info("Claim marked as under review: {}", claim.getClaimNumber());
    }

    /**
     * Mark approved claim as paid (Admin only)
     */
    @Transactional
    public void markAsPaid(UUID claimId) {
        log.info("Marking claim as paid: {}", claimId);

        Claim claim = claimRepository.findById(claimId)
                .orElseThrow(() -> new ResourceNotFoundException("Claim", "id", claimId));

        claim.markAsPaid();
        claimRepository.save(claim);

        log.info("Claim marked as paid: {}", claim.getClaimNumber());
    }

    /**
     * Get claim statistics (Admin only)
     */
    @Transactional(readOnly = true)
    public ClaimStatistics getClaimStatistics() {
        log.debug("Fetching claim statistics");

        long totalClaims = claimRepository.count();
        long pendingClaims = claimRepository.countByStatus(ClaimStatus.PENDING);
        long underReviewClaims = claimRepository.countByStatus(ClaimStatus.UNDER_REVIEW);
        long approvedClaims = claimRepository.countByStatus(ClaimStatus.APPROVED);
        long rejectedClaims = claimRepository.countByStatus(ClaimStatus.REJECTED);
        long paidClaims = claimRepository.countByStatus(ClaimStatus.PAID);
        Double totalApprovedAmount = claimRepository.sumApprovedClaimAmounts();

        return ClaimStatistics.builder()
                .totalClaims(totalClaims)
                .pendingClaims(pendingClaims)
                .underReviewClaims(underReviewClaims)
                .approvedClaims(approvedClaims)
                .rejectedClaims(rejectedClaims)
                .paidClaims(paidClaims)
                .totalApprovedAmount(totalApprovedAmount != null ? totalApprovedAmount : 0.0)
                .build();
    }

    /**
     * Generate unique claim number
     */
    private String generateClaimNumber() {
        String year = String.valueOf(LocalDate.now().getYear());
        String uniquePart = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        return "CLM-" + year + "-" + uniquePart;
    }

    /**
     * Validate policy for claim submission
     */
    private void validatePolicyForClaim(UUID userId, UUID policyId) {
        log.debug("Validating policy {} for user {}", policyId, userId);

        try {
            // Call Policy Service to verify policy
            WebClient webClient = webClientBuilder.baseUrl("lb://policy-service").build();

            // Fetch policy details
            Map<String, Object> policyResponse = webClient.get()
                    .uri("/api/policies/{id}", policyId)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (policyResponse == null) {
                throw new ClaimException("Policy not found");
            }

            // Extract policy data from response
            Map<String, Object> policyData = (Map<String, Object>) policyResponse.get("data");
            if (policyData == null) {
                throw new ClaimException("Invalid policy response");
            }

            // Verify user owns the policy
            String policyUserId = (String) policyData.get("userId");
            if (!userId.toString().equals(policyUserId)) {
                throw new ClaimException("Policy does not belong to the current user");
            }

            // Verify policy is active
            String policyStatus = (String) policyData.get("status");
            if (!"ACTIVE".equals(policyStatus)) {
                throw new ClaimException("Policy is not active. Current status: " + policyStatus);
            }

            log.debug("Policy validation successful for policy: {}", policyId);

        } catch (ClaimException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to validate policy: {}", policyId, e);
            throw new ClaimException("Failed to validate policy. Please ensure the policy is valid and active.");
        }
    }

    /**
     * Notify user about claim submission
     */
    private void notifyClaimSubmitted(Claim claim) {
        log.info("Sending claim submission notification for: {}", claim.getClaimNumber());
        // Call Notification Service (async)
        // Implementation in Phase 5
    }

    /**
     * Notify user about claim review decision
     */
    private void notifyClaimReviewed(Claim claim) {
        log.info("Sending claim review notification for: {}", claim.getClaimNumber());
        // Call Notification Service (async)
        // Implementation in Phase 5
    }

    /**
     * Helper method to build PageResponse
     */
    private PageResponse<ClaimDto> buildPageResponse(Page<Claim> claimPage) {
        List<ClaimDto> dtos = claimMapper.toDtoList(claimPage.getContent());

        return PageResponse.<ClaimDto>builder()
                .content(dtos)
                .page(claimPage.getNumber())
                .size(claimPage.getSize())
                .totalElements(claimPage.getTotalElements())
                .totalPages(claimPage.getTotalPages())
                .isLast(claimPage.isLast())
                .build();
    }
}