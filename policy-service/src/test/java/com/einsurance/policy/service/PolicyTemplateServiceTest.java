package com.einsurance.policy.service;

import com.einsurance.common.dto.PolicyTemplateDto;
import com.einsurance.common.exception.ResourceNotFoundException;
import com.einsurance.policy.entity.PolicyTemplate;
import com.einsurance.policy.entity.PolicyTemplate.PolicyType;
import com.einsurance.policy.mapper.PolicyTemplateMapper;
import com.einsurance.policy.repository.PolicyTemplateRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for PolicyTemplateService
 */
@ExtendWith(MockitoExtension.class)
class PolicyTemplateServiceTest {

    @Mock
    private PolicyTemplateRepository policyTemplateRepository;

    @Mock
    private PolicyTemplateMapper policyTemplateMapper;

    @InjectMocks
    private PolicyTemplateService policyTemplateService;

    private PolicyTemplate testTemplate;
    private PolicyTemplateDto testTemplateDto;

    @BeforeEach
    void setUp() {
        testTemplate = PolicyTemplate.builder()
                .id(UUID.randomUUID())
                .name("Basic Life Insurance")
                .type(PolicyType.LIFE)
                .description("Comprehensive life insurance coverage")
                .price(new BigDecimal("500.00"))
                .coverageAmount(new BigDecimal("50000.00"))
                .durationMonths(12)
                .isActive(true)
                .build();

        testTemplateDto = PolicyTemplateDto.builder()
                .id(testTemplate.getId())
                .name(testTemplate.getName())
                .type(testTemplate.getType().name())
                .description(testTemplate.getDescription())
                .price(testTemplate.getPrice())
                .coverageAmount(testTemplate.getCoverageAmount())
                .durationMonths(testTemplate.getDurationMonths())
                .isActive(testTemplate.getIsActive())
                .build();
    }

    @Test
    void createPolicyTemplate_Success() {
        // Given
        when(policyTemplateMapper.toEntity(any(PolicyTemplateDto.class))).thenReturn(testTemplate);
        when(policyTemplateRepository.save(any(PolicyTemplate.class))).thenReturn(testTemplate);
        when(policyTemplateMapper.toDto(any(PolicyTemplate.class))).thenReturn(testTemplateDto);

        // When
        PolicyTemplateDto result = policyTemplateService.createPolicyTemplate(testTemplateDto);

        // Then
        assertNotNull(result);
        assertEquals(testTemplateDto.getName(), result.getName());
        verify(policyTemplateRepository, times(1)).save(any(PolicyTemplate.class));
    }

    @Test
    void getPolicyTemplateById_Success() {
        // Given
        UUID templateId = testTemplate.getId();
        when(policyTemplateRepository.findById(templateId)).thenReturn(Optional.of(testTemplate));
        when(policyTemplateMapper.toDto(any(PolicyTemplate.class))).thenReturn(testTemplateDto);

        // When
        PolicyTemplateDto result = policyTemplateService.getPolicyTemplateById(templateId);

        // Then
        assertNotNull(result);
        assertEquals(testTemplateDto.getId(), result.getId());
    }

    @Test
    void getPolicyTemplateById_ThrowsException_WhenNotFound() {
        // Given
        UUID templateId = UUID.randomUUID();
        when(policyTemplateRepository.findById(templateId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class,
                () -> policyTemplateService.getPolicyTemplateById(templateId));
    }

    @Test
    void getAllActivePolicyTemplates_Success() {
        // Given
        List<PolicyTemplate> templates = List.of(testTemplate);
        when(policyTemplateRepository.findByIsActiveTrue()).thenReturn(templates);
        when(policyTemplateMapper.toDtoList(anyList())).thenReturn(List.of(testTemplateDto));

        // When
        List<PolicyTemplateDto> result = policyTemplateService.getAllActivePolicyTemplates();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testTemplateDto.getName(), result.get(0).getName());
    }

    @Test
    void deactivatePolicyTemplate_Success() {
        // Given
        UUID templateId = testTemplate.getId();
        when(policyTemplateRepository.findById(templateId)).thenReturn(Optional.of(testTemplate));
        when(policyTemplateRepository.save(any(PolicyTemplate.class))).thenReturn(testTemplate);

        // When
        policyTemplateService.deactivatePolicyTemplate(templateId);

        // Then
        verify(policyTemplateRepository, times(1)).save(any(PolicyTemplate.class));
        assertFalse(testTemplate.getIsActive());
    }
}
