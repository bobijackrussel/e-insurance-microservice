package com.einsurance.common.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Payload to initiate a policy purchase.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PolicyPurchaseRequest {

    @NotNull
    private UUID policyTemplateId;
}
