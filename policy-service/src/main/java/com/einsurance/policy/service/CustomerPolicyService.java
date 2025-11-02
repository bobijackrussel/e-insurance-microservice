package com.einsurance.policy.service;

import com.einsurance.common.dto.CustomerPolicyDto;
import com.einsurance.common.dto.PageResponse;
import com.einsurance.common.exception.PolicyException;
import com.einsurance.common.exception.ResourceNotFoundException;
import com.einsurance.common.security.SecurityUtil;
import com.einsurance.policy.entity.CustomerPolicy;
import com.einsurance.policy.entity.CustomerPolicy.PolicyStatus;
import com.einsurance.policy.entity.PolicyTemplate;
import com.einsurance.policy.mapper.CustomerPolicyMapper;
import com.einsurance.policy.repository.CustomerPolicyRepository;
import com.einsurance.policy.repository.PolicyTemplateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Service for CustomerPolicy operations
 * Manages customer policy purchases and lifecycle
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerPolicyService {

    private final CustomerPolicyRepository customerPolicyRepository;
    private final PolicyTemplateRepository policyTemplateRepository;
    private final CustomerPolicyMapper customerPolicyMapper;

    /**
     * Initiate policy purchase (before payment)
     * Creates a pending policy record
     */
    @Transactional
    public CustomerPolicyDto initiatePolicyPurchase(UUID policyTemplateId, UUID userId) {
        log.info("Initiating policy purchase for user: {} and template: {}", userId, policyTemplateId);

        // Get policy template
        PolicyTemplate template = policyTemplateRepository.findById(policyTemplateId)
                .orElseThrow(() -> new ResourceNotFoundException("PolicyTemplate", "id", policyTemplateId));

        // Validate template is active
        if (!template.isAvailableForPurchase()) {
            throw new PolicyException("Policy template is not available for purchase");
        }

        // Check if user already has active policy of this type
        if (customerPolicyRepository.hasActivePolicyForTemplate(userId, policyTemplateId)) {
            throw new PolicyException("You already have an active policy of this type");
        }

        // Create customer policy
        LocalDate startDate = LocalDate.now();
        LocalDate expiryDate = startDate.plusMonths(template.getDurationMonths());

        CustomerPolicy customerPolicy = CustomerPolicy.builder()
                .userId(userId)
                .policyTemplate(template)
                .startDate(startDate)
                .expiryDate(expiryDate)
                .status(PolicyStatus.PENDING)  // PENDING until payment confirmed
                .totalAmount(template.getPrice())
                .build();

        // Generate policy number
        customerPolicy.setPolicyNumber(generatePolicyNumber());

        CustomerPolicy savedPolicy = customerPolicyRepository.save(customerPolicy);
        log.info("Policy purchase initiated with ID: {}", savedPolicy.getId());

        return customerPolicyMapper.toDto(savedPolicy);
    }

    /**
     * Confirm policy purchase (after successful payment)
     */
    @Transactional
    public CustomerPolicyDto confirmPolicyPurchase(UUID policyId, UUID transactionId) {
        log.info("Confirming policy purchase: {} with transaction: {}", policyId, transactionId);

        CustomerPolicy policy = customerPolicyRepository.findById(policyId)
                .orElseThrow(() -> new ResourceNotFoundException("CustomerPolicy", "id", policyId));

        policy.setPaymentTransactionId(transactionId);
        policy.setStatus(PolicyStatus.ACTIVE);

        CustomerPolicy confirmedPolicy = customerPolicyRepository.save(policy);
        log.info("Policy purchase confirmed: {}", policyId);

        return customerPolicyMapper.toDto(confirmedPolicy);
    }

    /**
     * Get customer policy by ID
     */
    @Transactional(readOnly = true)
    public CustomerPolicyDto getCustomerPolicyById(UUID id) {
        log.debug("Fetching customer policy by ID: {}", id);

        CustomerPolicy policy = customerPolicyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("CustomerPolicy", "id", id));

        // Check authorization - user can only view their own policies
        String currentUserId = SecurityUtil.getCurrentUserIdOrThrow();
        if (!policy.getUserId().toString().equals(currentUserId) && !SecurityUtil.isAdmin()) {
            throw new PolicyException("You don't have permission to view this policy");
        }

        return customerPolicyMapper.toDto(policy);
    }

    /**
     * Get customer policy by policy number
     */
    @Transactional(readOnly = true)
    public CustomerPolicyDto getCustomerPolicyByPolicyNumber(String policyNumber) {
        log.debug("Fetching customer policy by policy number: {}", policyNumber);

        CustomerPolicy policy = customerPolicyRepository.findByPolicyNumber(policyNumber)
                .orElseThrow(() -> new ResourceNotFoundException("CustomerPolicy", "policyNumber", policyNumber));

        // Check authorization
        String currentUserId = SecurityUtil.getCurrentUserIdOrThrow();
        if (!policy.getUserId().toString().equals(currentUserId) && !SecurityUtil.isAdmin()) {
            throw new PolicyException("You don't have permission to view this policy");
        }

        return customerPolicyMapper.toDto(policy);
    }

    /**
     * Get current user's policies
     */
    @Transactional(readOnly = true)
    public List<CustomerPolicyDto> getMyPolicies() {
        String currentUserId = SecurityUtil.getCurrentUserIdOrThrow();
        UUID userId = UUID.fromString(currentUserId);

        log.debug("Fetching policies for user: {}", userId);

        List<CustomerPolicy> policies = customerPolicyRepository.findByUserId(userId);
        return customerPolicyMapper.toDtoList(policies);
    }

    /**
     * Get current user's active policies
     */
    @Transactional(readOnly = true)
    public List<CustomerPolicyDto> getMyActivePolicies() {
        String currentUserId = SecurityUtil.getCurrentUserIdOrThrow();
        UUID userId = UUID.fromString(currentUserId);

        log.debug("Fetching active policies for user: {}", userId);

        List<CustomerPolicy> policies = customerPolicyRepository.findActiveByUserId(userId);
        return customerPolicyMapper.toDtoList(policies);
    }

    /**
     * Get policies by user ID (Admin only)
     */
    @Transactional(readOnly = true)
    public PageResponse<CustomerPolicyDto> getPoliciesByUserId(UUID userId, int page, int size) {
        log.debug("Admin fetching policies for user: {} - page: {}, size: {}", userId, page, size);

        Pageable pageable = PageRequest.of(page, size, Sort.by("purchaseDate").descending());
        Page<CustomerPolicy> policyPage = customerPolicyRepository.findByUserId(userId, pageable);

        return buildPageResponse(policyPage);
    }

    /**
     * Get all policies with pagination (Admin only)
     */
    @Transactional(readOnly = true)
    public PageResponse<CustomerPolicyDto> getAllPolicies(int page, int size) {
        log.debug("Admin fetching all policies - page: {}, size: {}", page, size);

        Pageable pageable = PageRequest.of(page, size, Sort.by("purchaseDate").descending());
        Page<CustomerPolicy> policyPage = customerPolicyRepository.findAll(pageable);

        return buildPageResponse(policyPage);
    }

    /**
     * Get policies by status (Admin only)
     */
    @Transactional(readOnly = true)
    public PageResponse<CustomerPolicyDto> getPoliciesByStatus(String status, int page, int size) {
        log.debug("Admin fetching policies by status: {} - page: {}, size: {}", status, page, size);

        PolicyStatus policyStatus = PolicyStatus.valueOf(status.toUpperCase());
        Pageable pageable = PageRequest.of(page, size, Sort.by("purchaseDate").descending());
        Page<CustomerPolicy> policyPage = customerPolicyRepository.findByStatus(policyStatus, pageable);

        return buildPageResponse(policyPage);
    }

    /**
     * Cancel policy (Customer or Admin)
     */
    @Transactional
    public void cancelPolicy(UUID policyId) {
        log.info("Cancelling policy: {}", policyId);

        CustomerPolicy policy = customerPolicyRepository.findById(policyId)
                .orElseThrow(() -> new ResourceNotFoundException("CustomerPolicy", "id", policyId));

        // Check authorization
        String currentUserId = SecurityUtil.getCurrentUserIdOrThrow();
        if (!policy.getUserId().toString().equals(currentUserId) && !SecurityUtil.isAdmin()) {
            throw new PolicyException("You don't have permission to cancel this policy");
        }

        // Validate policy can be cancelled
        if (policy.getStatus() == PolicyStatus.CANCELLED) {
            throw new PolicyException("Policy is already cancelled");
        }

        if (policy.getStatus() == PolicyStatus.EXPIRED) {
            throw new PolicyException("Cannot cancel expired policy");
        }

        policy.setStatus(PolicyStatus.CANCELLED);
        customerPolicyRepository.save(policy);

        log.info("Policy cancelled successfully: {}", policyId);
    }

    /**
     * Suspend policy (Admin only)
     */
    @Transactional
    public void suspendPolicy(UUID policyId) {
        log.info("Suspending policy: {}", policyId);

        CustomerPolicy policy = customerPolicyRepository.findById(policyId)
                .orElseThrow(() -> new ResourceNotFoundException("CustomerPolicy", "id", policyId));

        if (policy.getStatus() != PolicyStatus.ACTIVE) {
            throw new PolicyException("Only active policies can be suspended");
        }

        policy.setStatus(PolicyStatus.SUSPENDED);
        customerPolicyRepository.save(policy);

        log.info("Policy suspended successfully: {}", policyId);
    }

    /**
     * Reactivate suspended policy (Admin only)
     */
    @Transactional
    public void reactivatePolicy(UUID policyId) {
        log.info("Reactivating policy: {}", policyId);

        CustomerPolicy policy = customerPolicyRepository.findById(policyId)
                .orElseThrow(() -> new ResourceNotFoundException("CustomerPolicy", "id", policyId));

        if (policy.getStatus() != PolicyStatus.SUSPENDED) {
            throw new PolicyException("Only suspended policies can be reactivated");
        }

        if (policy.isExpired()) {
            throw new PolicyException("Cannot reactivate expired policy");
        }

        policy.setStatus(PolicyStatus.ACTIVE);
        customerPolicyRepository.save(policy);

        log.info("Policy reactivated successfully: {}", policyId);
    }

    /**
     * Get customer policy statistics (Admin only)
     */
    @Transactional(readOnly = true)
    public CustomerPolicyStatistics getPolicyStatistics() {
        log.debug("Fetching customer policy statistics");

        long totalPolicies = customerPolicyRepository.count();
        long activePolicies = customerPolicyRepository.findAll().stream()
                .filter(p -> p.getStatus() == PolicyStatus.ACTIVE)
                .count();
        long expiredPolicies = customerPolicyRepository.findAll().stream()
                .filter(p -> p.getStatus() == PolicyStatus.EXPIRED)
                .count();
        long cancelledPolicies = customerPolicyRepository.findAll().stream()
                .filter(p -> p.getStatus() == PolicyStatus.CANCELLED)
                .count();
        long suspendedPolicies = customerPolicyRepository.findAll().stream()
                .filter(p -> p.getStatus() == PolicyStatus.SUSPENDED)
                .count();

        return CustomerPolicyStatistics.builder()
                .totalPolicies(totalPolicies)
                .activePolicies(activePolicies)
                .expiredPolicies(expiredPolicies)
                .cancelledPolicies(cancelledPolicies)
                .suspendedPolicies(suspendedPolicies)
                .build();
    }

    /**
     * Scheduled task to update expired policies
     * Runs daily at midnight
     */
    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void updateExpiredPolicies() {
        log.info("Running scheduled task to update expired policies");

        List<CustomerPolicy> expiredPolicies = customerPolicyRepository.findExpiredPolicies();
        
        for (CustomerPolicy policy : expiredPolicies) {
            policy.setStatus(PolicyStatus.EXPIRED);
            customerPolicyRepository.save(policy);
            log.debug("Policy expired: {}", policy.getPolicyNumber());
        }

        log.info("Updated {} expired policies", expiredPolicies.size());
    }

    /**
     * Generate unique policy number
     */
    private String generatePolicyNumber() {
        String year = String.valueOf(LocalDate.now().getYear());
        String uniquePart = java.util.UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        return "POL-" + year + "-" + uniquePart;
    }

    /**
     * Helper method to build PageResponse
     */
    private PageResponse<CustomerPolicyDto> buildPageResponse(Page<CustomerPolicy> policyPage) {
        List<CustomerPolicyDto> dtos = customerPolicyMapper.toDtoList(policyPage.getContent());

        return PageResponse.<CustomerPolicyDto>builder()
                .content(dtos)
                .page(policyPage.getNumber())
                .size(policyPage.getSize())
                .totalElements(policyPage.getTotalElements())
                .totalPages(policyPage.getTotalPages())
                .isLast(policyPage.isLast())
                .build();
    }
}