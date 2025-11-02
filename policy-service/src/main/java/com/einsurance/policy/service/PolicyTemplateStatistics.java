package com.einsurance.policy.service;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PolicyTemplateStatistics {
    private Long totalTemplates;
    private Long activeTemplates;
    private Long lifeTemplates;
    private Long travelTemplates;
    private Long propertyTemplates;
}
