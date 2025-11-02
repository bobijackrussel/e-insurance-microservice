package com.einsurance.common.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Payload for submitting a new claim.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClaimSubmissionRequest {

    @NotNull
    private UUID customerPolicyId;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = false)
    private BigDecimal amount;

    @NotBlank
    private String description;

    @NotNull
    private LocalDate incidentDate;
}
