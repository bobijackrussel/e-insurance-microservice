package com.einsurance.policy.controller;

import com.einsurance.common.dto.ApiResponse;
import com.einsurance.common.dto.CustomerPolicyDto;
import com.einsurance.common.dto.PageResponse;
import com.einsurance.common.dto.PolicyPurchaseRequest;
import com.einsurance.common.security.SecurityUtil;
import com.einsurance.policy.service.CustomerPolicyService;
import com.einsurance.policy.service.CustomerPolicyStatistics;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/policies")
@RequiredArgsConstructor
@Tag(name = "Customer Policies", description = "APIs for managing customer policy purchases")
@SecurityRequirement(name = "bearerAuth")
public class CustomerPolicyController {

    private final CustomerPolicyService customerPolicyService;

    @PostMapping("/purchase/initiate")
    @Operation(summary = "Initiate purchase", description = "Initiate policy purchase (before payment)")
    public ApiResponse<CustomerPolicyDto> initiatePurchase(@Valid @RequestBody PolicyPurchaseRequest request) {
        String userId = SecurityUtil.getCurrentUserIdOrThrow();
        log.info("User {} initiating policy purchase for template: {}", userId, request.getPolicyTemplateId());

        CustomerPolicyDto policy = customerPolicyService.initiatePolicyPurchase(
                request.getPolicyTemplateId(),
                UUID.fromString(userId)
        );
        return ApiResponse.success("Policy purchase initiated", policy);
    }

    @PostMapping("/purchase/confirm/{policyId}")
    @Operation(summary = "Confirm purchase", description = "Confirm policy purchase after successful payment")
    public ApiResponse<CustomerPolicyDto> confirmPurchase(
            @PathVariable UUID policyId,
            @RequestParam UUID transactionId) {
        log.info("Confirming policy purchase: {} with transaction: {}", policyId, transactionId);

        CustomerPolicyDto policy = customerPolicyService.confirmPolicyPurchase(policyId, transactionId);
        return ApiResponse.success("Policy purchase confirmed", policy);
    }

    @GetMapping("/my-policies")
    @Operation(summary = "Get my policies", description = "Get current user's policies")
    public ApiResponse<List<CustomerPolicyDto>> getMyPolicies() {
        log.info("Fetching current user's policies");
        List<CustomerPolicyDto> policies = customerPolicyService.getMyPolicies();
        return ApiResponse.success(policies);
    }

    @GetMapping("/my-policies/active")
    @Operation(summary = "Get my active policies", description = "Get current user's active policies")
    public ApiResponse<List<CustomerPolicyDto>> getMyActivePolicies() {
        log.info("Fetching current user's active policies");
        List<CustomerPolicyDto> policies = customerPolicyService.getMyActivePolicies();
        return ApiResponse.success(policies);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get policy", description = "Get customer policy by ID")
    public ApiResponse<CustomerPolicyDto> getPolicyById(@PathVariable UUID id) {
        log.info("Fetching customer policy: {}", id);
        CustomerPolicyDto policy = customerPolicyService.getCustomerPolicyById(id);
        return ApiResponse.success(policy);
    }

    @GetMapping("/policy-number/{policyNumber}")
    @Operation(summary = "Get policy by number", description = "Get customer policy by policy number")
    public ApiResponse<CustomerPolicyDto> getPolicyByNumber(@PathVariable String policyNumber) {
        log.info("Fetching customer policy by number: {}", policyNumber);
        CustomerPolicyDto policy = customerPolicyService.getCustomerPolicyByPolicyNumber(policyNumber);
        return ApiResponse.success(policy);
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get user policies", description = "Get policies for specific user (Admin only)")
    public ApiResponse<PageResponse<CustomerPolicyDto>> getUserPolicies(
            @PathVariable UUID userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("Admin fetching policies for user: {}", userId);
        PageResponse<CustomerPolicyDto> policies = customerPolicyService.getPoliciesByUserId(userId, page, size);
        return ApiResponse.success(policies);
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all policies", description = "Get all customer policies (Admin only)")
    public ApiResponse<PageResponse<CustomerPolicyDto>> getAllPolicies(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("Admin fetching all policies");
        PageResponse<CustomerPolicyDto> policies = customerPolicyService.getAllPolicies(page, size);
        return ApiResponse.success(policies);
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get policies by status", description = "Get policies filtered by status (Admin only)")
    public ApiResponse<PageResponse<CustomerPolicyDto>> getPoliciesByStatus(
            @PathVariable String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("Admin fetching policies by status: {}", status);
        PageResponse<CustomerPolicyDto> policies = customerPolicyService.getPoliciesByStatus(status, page, size);
        return ApiResponse.success(policies);
    }

    @PatchMapping("/{id}/cancel")
    @Operation(summary = "Cancel policy", description = "Cancel customer policy")
    public ApiResponse<Void> cancelPolicy(@PathVariable UUID id) {
        log.info("Cancelling policy: {}", id);
        customerPolicyService.cancelPolicy(id);
        return ApiResponse.success("Policy cancelled successfully");
    }

    @PatchMapping("/{id}/suspend")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Suspend policy", description = "Suspend customer policy (Admin only)")
    public ApiResponse<Void> suspendPolicy(@PathVariable UUID id) {
        log.info("Admin suspending policy: {}", id);
        customerPolicyService.suspendPolicy(id);
        return ApiResponse.success("Policy suspended successfully");
    }

    @PatchMapping("/{id}/reactivate")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Reactivate policy", description = "Reactivate suspended policy (Admin only)")
    public ApiResponse<Void> reactivatePolicy(@PathVariable UUID id) {
        log.info("Admin reactivating policy: {}", id);
        customerPolicyService.reactivatePolicy(id);
        return ApiResponse.success("Policy reactivated successfully");
    }

    @GetMapping("/statistics")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get statistics", description = "Get customer policy statistics (Admin only)")
    public ApiResponse<CustomerPolicyStatistics> getStatistics() {
        log.info("Fetching customer policy statistics");
        CustomerPolicyStatistics stats = customerPolicyService.getPolicyStatistics();
        return ApiResponse.success(stats);
    }
}
