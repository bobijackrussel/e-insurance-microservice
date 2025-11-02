package com.einsurance.claims.mapper;

import com.einsurance.claims.entity.Claim;
import com.einsurance.common.dto.ClaimDto;
import com.einsurance.common.dto.ClaimSubmissionRequest;
import org.mapstruct.*;

import java.util.List;

/**
 * MapStruct mapper for Claim entity and DTOs
 */
@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface ClaimMapper {

    /**
     * Convert entity to DTO
     */
    @Mapping(target = "status", source = "status")
    ClaimDto toDto(Claim claim);

    /**
     * Convert list of entities to DTOs
     */
    List<ClaimDto> toDtoList(List<Claim> claims);

    /**
     * Convert submission request to entity
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "claimNumber", ignore = true)
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "submittedDate", ignore = true)
    @Mapping(target = "reviewedDate", ignore = true)
    @Mapping(target = "reviewedBy", ignore = true)
    @Mapping(target = "adminNotes", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Claim toEntity(ClaimSubmissionRequest request);
}