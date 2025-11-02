package com.einsurance.policy.controller;

import com.einsurance.common.dto.ApiResponse;
import com.einsurance.common.dto.PageResponse;
import com.einsurance.common.dto.PolicyTemplateDto;
import com.einsurance.policy.service.PolicyTemplateService;
import com.einsurance.policy.service.PolicyTemplateStatistics;
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

@Slf4j
@RestController
@RequestMapping("/api/policies/templates")
@RequiredArgsConstructor
@Tag(name = "Policy Templates", description = "APIs for managing insurance policy catalog")
@SecurityRequirement(name = "bearerAuth")
public class PolicyTemplateController {

    private final PolicyTemplateService policyTemplateService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create policy template", description = "Create new insurance policy template (Admin only)")
    public ApiResponse<PolicyTemplateDto> createPolicyTemplate(@Valid @RequestBody PolicyTemplateDto dto) {
        log.info("Creating new policy template: {}", dto.getName());
        PolicyTemplateDto created = policyTemplateService.createPolicyTemplate(dto);
        return ApiResponse.success("Policy template created successfully", created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update policy template", description = "Update policy template (Admin only)")
    public ApiResponse<PolicyTemplateDto> updatePolicyTemplate(
            @PathVariable UUID id,
            @Valid @RequestBody PolicyTemplateDto dto) {
        log.info("Updating policy template: {}", id);
        PolicyTemplateDto updated = policyTemplateService.updatePolicyTemplate(id, dto);
        return ApiResponse.success("Policy template updated successfully", updated);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get policy template", description = "Get policy template by ID")
    public ApiResponse<PolicyTemplateDto> getPolicyTemplateById(@PathVariable UUID id) {
        log.info("Fetching policy template: {}", id);
        PolicyTemplateDto template = policyTemplateService.getPolicyTemplateById(id);
        return ApiResponse.success(template);
    }

    @GetMapping("/active")
    @Operation(summary = "Get active templates", description = "Get all active policy templates (Public)")
    public ApiResponse<List<PolicyTemplateDto>> getAllActiveTemplates() {
        log.info("Fetching all active policy templates");
        List<PolicyTemplateDto> templates = policyTemplateService.getAllActivePolicyTemplates();
        return ApiResponse.success(templates);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all templates", description = "Get all policy templates with pagination (Admin only)")
    public ApiResponse<PageResponse<PolicyTemplateDto>> getAllTemplates(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("Admin fetching all policy templates - page: {}, size: {}", page, size);
        PageResponse<PolicyTemplateDto> templates = policyTemplateService.getAllPolicyTemplates(page, size);
        return ApiResponse.success(templates);
    }

    @GetMapping("/type/{type}")
    @Operation(summary = "Get templates by type", description = "Get policy templates filtered by type")
    public ApiResponse<List<PolicyTemplateDto>> getTemplatesByType(@PathVariable String type) {
        log.info("Fetching policy templates by type: {}", type);
        List<PolicyTemplateDto> templates = policyTemplateService.getPolicyTemplatesByType(type);
        return ApiResponse.success(templates);
    }

    @GetMapping("/type/{type}/paginated")
    @Operation(summary = "Get templates by type (paginated)", description = "Get policy templates by type with pagination")
    public ApiResponse<PageResponse<PolicyTemplateDto>> getTemplatesByTypePaginated(
            @PathVariable String type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("Fetching policy templates by type: {} - page: {}, size: {}", type, page, size);
        PageResponse<PolicyTemplateDto> templates = policyTemplateService.getPolicyTemplatesByType(type, page, size);
        return ApiResponse.success(templates);
    }

    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Deactivate template", description = "Deactivate policy template (Admin only)")
    public ApiResponse<Void> deactivateTemplate(@PathVariable UUID id) {
        log.info("Deactivating policy template: {}", id);
        policyTemplateService.deactivatePolicyTemplate(id);
        return ApiResponse.success("Policy template deactivated successfully");
    }

    @PatchMapping("/{id}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Activate template", description = "Activate policy template (Admin only)")
    public ApiResponse<Void> activateTemplate(@PathVariable UUID id) {
        log.info("Activating policy template: {}", id);
        policyTemplateService.activatePolicyTemplate(id);
        return ApiResponse.success("Policy template activated successfully");
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete template", description = "Permanently delete policy template (Admin only)")
    public ApiResponse<Void> deleteTemplate(@PathVariable UUID id) {
        log.warn("Admin deleting policy template: {}", id);
        policyTemplateService.deletePolicyTemplate(id);
        return ApiResponse.success("Policy template deleted successfully");
    }

    @GetMapping("/statistics")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get statistics", description = "Get policy template statistics (Admin only)")
    public ApiResponse<PolicyTemplateStatistics> getStatistics() {
        log.info("Fetching policy template statistics");
        PolicyTemplateStatistics stats = policyTemplateService.getPolicyTemplateStatistics();
        return ApiResponse.success(stats);
    }
}
