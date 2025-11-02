package com.einsurance.claims.controller;

import com.einsurance.claims.service.ClaimStatistics;
import com.einsurance.claims.service.ClaimsService;
import com.einsurance.common.dto.ApiResponse;
import com.einsurance.common.dto.ClaimDto;
import com.einsurance.common.dto.ClaimReviewRequest;
import com.einsurance.common.dto.ClaimSubmissionRequest;
import com.einsurance.common.dto.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for Claims operations
 */
@Slf4j
@RestController
@RequestMapping("/api/claims")
@RequiredArgsConstructor
@Tag(name = "Claims", description = "APIs for insurance claim management")
@SecurityRequirement(name = "bearerAuth")
public class ClaimsController {

    private final ClaimsService claimsService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Submit claim", description = "Submit new insurance claim for a policy")
    public ApiResponse<ClaimDto> submitClaim(@Valid @RequestBody ClaimSubmissionRequest request) {
        log.info("Received claim submission for policy: {}", request.getCustomerPolicyId());
        ClaimDto claim = claimsService.submitClaim(request);
        return ApiResponse.success("Claim submitted successfully", claim);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get claim", description = "Get claim by ID")
    public ApiResponse<ClaimDto> getClaimById(@PathVariable UUID id) {
        log.info("Fetching claim: {}", id);
        ClaimDto claim = claimsService.getClaimById(id);
        return ApiResponse.success(claim);
    }

    @GetMapping("/claim-number/{claimNumber}")
    @Operation(summary = "Get claim by number", description = "Get claim by claim number")
    public ApiResponse<ClaimDto> getClaimByNumber(@PathVariable String claimNumber) {
        log.info("Fetching claim by number: {}", claimNumber);
        ClaimDto claim = claimsService.getClaimByClaimNumber(claimNumber);
        return ApiResponse.success(claim);
    }

    @GetMapping("/my-claims")
    @Operation(summary = "Get my claims", description = "Get current user's claims")
    public ApiResponse<List<ClaimDto>> getMyClaims() {
        log.info("Fetching current user's claims");
        List<ClaimDto> claims = claimsService.getMyClaims();
        return ApiResponse.success(claims);
    }

    @GetMapping("/my-claims/history")
    @Operation(summary = "Get claim history", description = "Get paginated claim history")
    public ApiResponse<PageResponse<ClaimDto>> getMyClaimsHistory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("Fetching claim history - page: {}, size: {}", page, size);
        PageResponse<ClaimDto> history = claimsService.getMyClaimsHistory(page, size);
        return ApiResponse.success(history);
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all claims", description = "Get all claims (Admin only)")
    public ApiResponse<PageResponse<ClaimDto>> getAllClaims(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("Admin fetching all claims - page: {}, size: {}", page, size);
        PageResponse<ClaimDto> claims = claimsService.getAllClaims(page, size);
        return ApiResponse.success(claims);
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get claims by status", description = "Get claims filtered by status (Admin only)")
    public ApiResponse<PageResponse<ClaimDto>> getClaimsByStatus(
            @PathVariable String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("Admin fetching claims by status: {} - page: {}, size: {}", status, page, size);
        PageResponse<ClaimDto> claims = claimsService.getClaimsByStatus(status, page, size);
        return ApiResponse.success(claims);
    }

    @GetMapping("/pending")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get pending claims", description = "Get pending claims for review (Admin only)")
    public ApiResponse<PageResponse<ClaimDto>> getPendingClaims(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("Admin fetching pending claims - page: {}, size: {}", page, size);
        PageResponse<ClaimDto> claims = claimsService.getPendingClaims(page, size);
        return ApiResponse.success(claims);
    }

    @PutMapping("/{id}/review")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Review claim", description = "Approve or reject claim (Admin only)")
    public ApiResponse<ClaimDto> reviewClaim(
            @PathVariable UUID id,
            @Valid @RequestBody ClaimReviewRequest request) {
        log.info("Admin reviewing claim: {} with status: {}", id, request.getStatus());
        ClaimDto claim = claimsService.reviewClaim(id, request);
        return ApiResponse.success("Claim reviewed successfully", claim);
    }

    @PatchMapping("/{id}/under-review")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Mark as under review", description = "Mark claim as under review (Admin only)")
    public ApiResponse<Void> markAsUnderReview(@PathVariable UUID id) {
        log.info("Admin marking claim as under review: {}", id);
        claimsService.markAsUnderReview(id);
        return ApiResponse.success("Claim marked as under review");
    }

    @PatchMapping("/{id}/paid")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Mark as paid", description = "Mark approved claim as paid (Admin only)")
    public ApiResponse<Void> markAsPaid(@PathVariable UUID id) {
        log.info("Admin marking claim as paid: {}", id);
        claimsService.markAsPaid(id);
        return ApiResponse.success("Claim marked as paid");
    }

    @GetMapping("/statistics")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get statistics", description = "Get claim statistics (Admin only)")
    public ApiResponse<ClaimStatistics> getStatistics() {
        log.info("Fetching claim statistics");
        ClaimStatistics stats = claimsService.getClaimStatistics();
        return ApiResponse.success(stats);
    }
}