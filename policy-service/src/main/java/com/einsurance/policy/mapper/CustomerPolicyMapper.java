package com.einsurance.policy.mapper;

import com.einsurance.common.dto.CustomerPolicyDto;
import com.einsurance.policy.entity.CustomerPolicy;
import com.einsurance.policy.entity.PolicyTemplate;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CustomerPolicyMapper {

    @Mapping(target = "policyTemplateId", source = "policyTemplate.id")
    @Mapping(target = "policyTemplateName", source = "policyTemplate.name")
    @Mapping(target = "policyType", expression = "java(mapPolicyType(policy.getPolicyTemplate()))")
    @Mapping(target = "remainingDays", expression = "java(mapRemainingDays(policy))")
    CustomerPolicyDto toDto(CustomerPolicy policy);

    List<CustomerPolicyDto> toDtoList(List<CustomerPolicy> policies);

    default String mapPolicyType(PolicyTemplate template) {
        if (template == null || template.getType() == null) {
            return null;
        }
        return template.getType().name();
    }

    default Long mapRemainingDays(CustomerPolicy policy) {
        return policy != null ? policy.getRemainingDays() : null;
    }
}
