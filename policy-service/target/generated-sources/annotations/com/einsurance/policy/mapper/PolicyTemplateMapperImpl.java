package com.einsurance.policy.mapper;

import com.einsurance.common.dto.PolicyTemplateDto;
import com.einsurance.policy.entity.PolicyTemplate;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-11-02T16:38:24+0100",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 23.0.2 (Oracle Corporation)"
)
@Component
public class PolicyTemplateMapperImpl implements PolicyTemplateMapper {

    @Override
    public PolicyTemplateDto toDto(PolicyTemplate entity) {
        if ( entity == null ) {
            return null;
        }

        PolicyTemplateDto.PolicyTemplateDtoBuilder policyTemplateDto = PolicyTemplateDto.builder();

        policyTemplateDto.type( map( entity.getType() ) );
        policyTemplateDto.id( entity.getId() );
        policyTemplateDto.name( entity.getName() );
        policyTemplateDto.description( entity.getDescription() );
        policyTemplateDto.price( entity.getPrice() );
        policyTemplateDto.coverageAmount( entity.getCoverageAmount() );
        policyTemplateDto.durationMonths( entity.getDurationMonths() );
        policyTemplateDto.termsConditions( entity.getTermsConditions() );
        policyTemplateDto.isActive( entity.getIsActive() );
        policyTemplateDto.createdAt( entity.getCreatedAt() );
        policyTemplateDto.updatedAt( entity.getUpdatedAt() );

        return policyTemplateDto.build();
    }

    @Override
    public List<PolicyTemplateDto> toDtoList(List<PolicyTemplate> entities) {
        if ( entities == null ) {
            return null;
        }

        List<PolicyTemplateDto> list = new ArrayList<PolicyTemplateDto>( entities.size() );
        for ( PolicyTemplate policyTemplate : entities ) {
            list.add( toDto( policyTemplate ) );
        }

        return list;
    }

    @Override
    public PolicyTemplate toEntity(PolicyTemplateDto dto) {
        if ( dto == null ) {
            return null;
        }

        PolicyTemplate.PolicyTemplateBuilder policyTemplate = PolicyTemplate.builder();

        policyTemplate.id( dto.getId() );
        policyTemplate.name( dto.getName() );
        policyTemplate.description( dto.getDescription() );
        policyTemplate.price( dto.getPrice() );
        policyTemplate.coverageAmount( dto.getCoverageAmount() );
        policyTemplate.durationMonths( dto.getDurationMonths() );
        policyTemplate.termsConditions( dto.getTermsConditions() );
        policyTemplate.isActive( dto.getIsActive() );
        policyTemplate.createdAt( dto.getCreatedAt() );
        policyTemplate.updatedAt( dto.getUpdatedAt() );

        policyTemplate.type( mapToPolicyType(dto.getType()) );

        return policyTemplate.build();
    }

    @Override
    public void updateEntityFromDto(PolicyTemplateDto dto, PolicyTemplate entity) {
        if ( dto == null ) {
            return;
        }

        entity.setId( dto.getId() );
        entity.setName( dto.getName() );
        entity.setDescription( dto.getDescription() );
        entity.setPrice( dto.getPrice() );
        entity.setCoverageAmount( dto.getCoverageAmount() );
        entity.setDurationMonths( dto.getDurationMonths() );
        entity.setTermsConditions( dto.getTermsConditions() );
        entity.setIsActive( dto.getIsActive() );
        entity.setCreatedAt( dto.getCreatedAt() );
        entity.setUpdatedAt( dto.getUpdatedAt() );

        entity.setType( mapToPolicyType(dto.getType()) );
    }
}
