package com.einsurance.policy.service;

import com.einsurance.common.dto.CustomerPolicyDto;
import com.einsurance.common.exception.PolicyException;
import com.einsurance.common.exception.ResourceNotFoundException;
import com.einsurance.policy.entity.CustomerPolicy;
import com.einsurance.policy.entity.CustomerPolicy.PolicyStatus;
import com.einsurance.policy.entity.PolicyTemplate;
import com.einsurance.policy.entity.PolicyTemplate.PolicyType;
import com.einsurance.policy.mapper.CustomerPolicyMapper;
import com.einsurance.policy.repository.CustomerPolicyRepository;
import com.einsurance.policy.repository.PolicyTemplateRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for CustomerPolicyService
 */
@ExtendWith(MockitoExtension.class)
class CustomerPolicyServiceTest {

    @Mock
    private CustomerPolicyRepository customerPolicyRepository;

    @Mock
    private PolicyTemplateRepository policyTemplateRepository;

    @Mock
    private CustomerPolicyMapper customerPolicyMapper;

    @InjectMocks
    private CustomerPolicyService customerPolicyService;

    private PolicyTemplate testTemplate;
    private CustomerPolicy testPolicy;
    private CustomerPolicyDto testPolicyDto;
    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();

        testTemplate = PolicyTemplate.builder()
                .id(UUID.randomUUID())
                .name("Travel Insurance")
                .type(PolicyType.TRAVEL)
                .price(new BigDecimal("50.00"))
                .coverageAmount(new BigDecimal("25000.00"))
                .durationMonths(1)
                .isActive(true)
                .build();

        testPolicy = CustomerPolicy.builder()
                .id(UUID.randomUUID())
                .policyNumber("POL-2025-001234")
                .userId(userId)
                .policyTemplate(testTemplate)
                .startDate(LocalDate.now())
                .expiryDate(LocalDate.now().plusMonths(1))
                .status(PolicyStatus.ACTIVE)
                .totalAmount(new BigDecimal("50.00"))
                .build();

        testPolicyDto = CustomerPolicyDto.builder()
                .id(testPolicy.getId())
                .policyNumber(testPolicy.getPolicyNumber())
                .userId(testPolicy.getUserId())
                .policyTemplateId(testTemplate.getId())
                .startDate(testPolicy.getStartDate())
                .expiryDate(testPolicy.getExpiryDate())
                .status(testPolicy.getStatus().name())
                .totalAmount(testPolicy.getTotalAmount())
                .build();
    }

    @Test
    void initiatePolicyPurchase_Success() {
        // Given
        when(policyTemplateRepository.findById(any(UUID.class))).thenReturn(Optional.of(testTemplate));
        when(customerPolicyRepository.hasActivePolicyForTemplate(any(UUID.class), any(UUID.class))).thenReturn(false);
        when(customerPolicyRepository.save(any(CustomerPolicy.class))).thenReturn(testPolicy);
        when(customerPolicyMapper.toDto(any(CustomerPolicy.class))).thenReturn(testPolicyDto);

        // When
        CustomerPolicyDto result = customerPolicyService.initiatePolicyPurchase(testTemplate.getId(), userId);

        // Then
        assertNotNull(result);
        assertEquals(testPolicyDto.getPolicyNumber(), result.getPolicyNumber());
        verify(customerPolicyRepository, times(1)).save(any(CustomerPolicy.class));
    }

    @Test
    void initiatePolicyPurchase_ThrowsException_WhenTemplateNotActive() {
        // Given
        testTemplate.setIsActive(false);
        when(policyTemplateRepository.findById(any(UUID.class))).thenReturn(Optional.of(testTemplate));

        // When & Then
        assertThrows(PolicyException.class,
                () -> customerPolicyService.initiatePolicyPurchase(testTemplate.getId(), userId));
        verify(customerPolicyRepository, never()).save(any(CustomerPolicy.class));
    }

    @Test
    void initiatePolicyPurchase_ThrowsException_WhenUserHasActivePolicy() {
        // Given
        when(policyTemplateRepository.findById(any(UUID.class))).thenReturn(Optional.of(testTemplate));
        when(customerPolicyRepository.hasActivePolicyForTemplate(any(UUID.class), any(UUID.class))).thenReturn(true);

        // When & Then
        assertThrows(PolicyException.class,
                () -> customerPolicyService.initiatePolicyPurchase(testTemplate.getId(), userId));
        verify(customerPolicyRepository, never()).save(any(CustomerPolicy.class));
    }

    @Test
    void confirmPolicyPurchase_Success() {
        // Given
        UUID transactionId = UUID.randomUUID();
        when(customerPolicyRepository.findById(any(UUID.class))).thenReturn(Optional.of(testPolicy));
        when(customerPolicyRepository.save(any(CustomerPolicy.class))).thenReturn(testPolicy);
        when(customerPolicyMapper.toDto(any(CustomerPolicy.class))).thenReturn(testPolicyDto);

        // When
        CustomerPolicyDto result = customerPolicyService.confirmPolicyPurchase(testPolicy.getId(), transactionId);

        // Then
        assertNotNull(result);
        assertEquals(testPolicyDto.getId(), result.getId());
        verify(customerPolicyRepository, times(1)).save(any(CustomerPolicy.class));
    }

    @Test
    void cancelPolicy_Success() {
        // Given
        when(customerPolicyRepository.findById(any(UUID.class))).thenReturn(Optional.of(testPolicy));
        when(customerPolicyRepository.save(any(CustomerPolicy.class))).thenReturn(testPolicy);

        // When
        customerPolicyService.cancelPolicy(testPolicy.getId());

        // Then
        verify(customerPolicyRepository, times(1)).save(any(CustomerPolicy.class));
        assertEquals(PolicyStatus.CANCELLED, testPolicy.getStatus());
    }

    @Test
    void cancelPolicy_ThrowsException_WhenAlreadyCancelled() {
        // Given
        testPolicy.setStatus(PolicyStatus.CANCELLED);
        when(customerPolicyRepository.findById(any(UUID.class))).thenReturn(Optional.of(testPolicy));

        // When & Then
        assertThrows(PolicyException.class,
                () -> customerPolicyService.cancelPolicy(testPolicy.getId()));
        verify(customerPolicyRepository, never()).save(any(CustomerPolicy.class));
    }
}
