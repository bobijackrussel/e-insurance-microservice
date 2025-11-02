package com.einsurance.policy.service;

import com.einsurance.common.dto.PageResponse;
import com.einsurance.common.dto.PolicyTemplateDto;
import com.einsurance.common.exception.ResourceNotFoundException;
import com.einsurance.policy.entity.PolicyTemplate;
import com.einsurance.policy.entity.PolicyTemplate.PolicyType;
import com.einsurance.policy.mapper.PolicyTemplateMapper;
import com.einsurance.policy.repository.PolicyTemplateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Service for PolicyTemplate operations
 * Manages insurance policy catalog
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PolicyTemplateService {

    private final PolicyTemplateRepository policyTemplateRepository;
    private final PolicyTemplateMapper policyTemplateMapper;

    /**
     * Create new policy template (Admin only)
     */
    @Transactional
    public PolicyTemplateDto createPolicyTemplate(PolicyTemplateDto dto) {
        log.info("Creating new policy template: {}", dto.getName());

        PolicyTemplate policyTemplate = policyTemplateMapper.toEntity(dto);
        PolicyTemplate savedTemplate = policyTemplateRepository.save(policyTemplate);

        log.info("Policy template created successfully with ID: {}", savedTemplate.getId());
        return policyTemplateMapper.toDto(savedTemplate);
    }

    /**
     * Update policy template (Admin only)
     */
    @Transactional
    public PolicyTemplateDto updatePolicyTemplate(UUID id, PolicyTemplateDto dto) {
        log.info("Updating policy template: {}", id);

        PolicyTemplate policyTemplate = policyTemplateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PolicyTemplate", "id", id));

        policyTemplateMapper.updateEntityFromDto(dto, policyTemplate);
        PolicyTemplate updatedTemplate = policyTemplateRepository.save(policyTemplate);

        log.info("Policy template updated successfully: {}", id);
        return policyTemplateMapper.toDto(updatedTemplate);
    }

    /**
     * Get policy template by ID
     */
    @Transactional(readOnly = true)
    public PolicyTemplateDto getPolicyTemplateById(UUID id) {
        log.debug("Fetching policy template by ID: {}", id);

        PolicyTemplate policyTemplate = policyTemplateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PolicyTemplate", "id", id));

        return policyTemplateMapper.toDto(policyTemplate);
    }

    /**
     * Get all active policy templates (public access)
     */
    @Transactional(readOnly = true)
    public List<PolicyTemplateDto> getAllActivePolicyTemplates() {
        log.debug("Fetching all active policy templates");

        List<PolicyTemplate> templates = policyTemplateRepository.findByIsActiveTrue();
        return policyTemplateMapper.toDtoList(templates);
    }

    /**
     * Get all policy templates with pagination (Admin only)
     */
    @Transactional(readOnly = true)
    public PageResponse<PolicyTemplateDto> getAllPolicyTemplates(int page, int size) {
        log.debug("Fetching all policy templates - page: {}, size: {}", page, size);

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<PolicyTemplate> templatePage = policyTemplateRepository.findAll(pageable);

        return buildPageResponse(templatePage);
    }

    /**
     * Get policy templates by type
     */
    @Transactional(readOnly = true)
    public List<PolicyTemplateDto> getPolicyTemplatesByType(String type) {
        log.debug("Fetching policy templates by type: {}", type);

        PolicyType policyType = PolicyType.valueOf(type.toUpperCase());
        List<PolicyTemplate> templates = policyTemplateRepository
                .findByTypeAndIsActiveTrue(policyType);

        return policyTemplateMapper.toDtoList(templates);
    }

    /**
     * Get policy templates by type with pagination
     */
    @Transactional(readOnly = true)
    public PageResponse<PolicyTemplateDto> getPolicyTemplatesByType(
            String type, int page, int size) {
        log.debug("Fetching policy templates by type: {} - page: {}, size: {}", type, page, size);

        PolicyType policyType = PolicyType.valueOf(type.toUpperCase());
        Pageable pageable = PageRequest.of(page, size, Sort.by("price").ascending());
        Page<PolicyTemplate> templatePage = policyTemplateRepository
                .findByTypeAndIsActiveTrue(policyType, pageable);

        return buildPageResponse(templatePage);
    }

    /**
     * Deactivate policy template (soft delete - Admin only)
     */
    @Transactional
    public void deactivatePolicyTemplate(UUID id) {
        log.info("Deactivating policy template: {}", id);

        PolicyTemplate policyTemplate = policyTemplateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PolicyTemplate", "id", id));

        policyTemplate.setIsActive(false);
        policyTemplateRepository.save(policyTemplate);

        log.info("Policy template deactivated successfully: {}", id);
    }

    /**
     * Activate policy template (Admin only)
     */
    @Transactional
    public void activatePolicyTemplate(UUID id) {
        log.info("Activating policy template: {}", id);

        PolicyTemplate policyTemplate = policyTemplateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PolicyTemplate", "id", id));

        policyTemplate.setIsActive(true);
        policyTemplateRepository.save(policyTemplate);

        log.info("Policy template activated successfully: {}", id);
    }

    /**
     * Delete policy template permanently (Admin only - use with caution)
     */
    @Transactional
    public void deletePolicyTemplate(UUID id) {
        log.warn("Permanently deleting policy template: {}", id);

        if (!policyTemplateRepository.existsById(id)) {
            throw new ResourceNotFoundException("PolicyTemplate", "id", id);
        }

        policyTemplateRepository.deleteById(id);
        log.warn("Policy template permanently deleted: {}", id);
    }

    /**
     * Get policy template statistics
     */
    @Transactional(readOnly = true)
    public PolicyTemplateStatistics getPolicyTemplateStatistics() {
        log.debug("Fetching policy template statistics");

        long totalTemplates = policyTemplateRepository.count();
        long activeTemplates = policyTemplateRepository.countByIsActiveTrue();
        long lifeTemplates = policyTemplateRepository.countByType(PolicyType.LIFE);
        long travelTemplates = policyTemplateRepository.countByType(PolicyType.TRAVEL);
        long propertyTemplates = policyTemplateRepository.countByType(PolicyType.PROPERTY);

        return PolicyTemplateStatistics.builder()
                .totalTemplates(totalTemplates)
                .activeTemplates(activeTemplates)
                .lifeTemplates(lifeTemplates)
                .travelTemplates(travelTemplates)
                .propertyTemplates(propertyTemplates)
                .build();
    }

    /**
     * Helper method to build PageResponse
     */
    private PageResponse<PolicyTemplateDto> buildPageResponse(Page<PolicyTemplate> templatePage) {
        List<PolicyTemplateDto> dtos = policyTemplateMapper.toDtoList(templatePage.getContent());

        return PageResponse.<PolicyTemplateDto>builder()
                .content(dtos)
                .page(templatePage.getNumber())
                .size(templatePage.getSize())
                .totalElements(templatePage.getTotalElements())
                .totalPages(templatePage.getTotalPages())
                .isLast(templatePage.isLast())
                .build();
    }
}