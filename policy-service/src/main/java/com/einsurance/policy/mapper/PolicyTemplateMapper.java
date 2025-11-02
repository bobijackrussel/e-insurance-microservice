package com.einsurance.policy.mapper;

import com.einsurance.common.dto.PolicyTemplateDto;
import com.einsurance.policy.entity.PolicyTemplate;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PolicyTemplateMapper {

    @Mapping(target = "type", source = "type")
    PolicyTemplateDto toDto(PolicyTemplate entity);

    List<PolicyTemplateDto> toDtoList(List<PolicyTemplate> entities);

    @Mapping(target = "type", expression = "java(mapToPolicyType(dto.getType()))")
    PolicyTemplate toEntity(PolicyTemplateDto dto);

    @Mapping(target = "type", expression = "java(mapToPolicyType(dto.getType()))")
    void updateEntityFromDto(PolicyTemplateDto dto, @MappingTarget PolicyTemplate entity);

    default String map(PolicyTemplate.PolicyType type) {
        return type != null ? type.name() : null;
    }

    default PolicyTemplate.PolicyType mapToPolicyType(String type) {
        if (type == null || type.isBlank()) {
            return null;
        }
        return PolicyTemplate.PolicyType.valueOf(type.toUpperCase());
    }
}
