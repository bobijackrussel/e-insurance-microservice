package com.einsurance.common.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Payload for approving or rejecting a claim.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClaimReviewRequest {

    /**
     * Expected values: APPROVED or REJECTED.
     */
    @NotBlank
    private String status;

    private String adminNotes;
}
