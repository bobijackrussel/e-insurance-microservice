package com.einsurance.payment.mapper;

import com.einsurance.common.dto.TransactionDto;
import com.einsurance.payment.entity.Transaction;
import org.mapstruct.*;

import java.util.List;

/**
 * MapStruct mapper for Transaction entity and DTOs
 */
@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface TransactionMapper {

    /**
     * Convert entity to DTO
     */
    @Mapping(target = "status", source = "status")
    TransactionDto toDto(Transaction transaction);

    /**
     * Convert list of entities to DTOs
     */
    List<TransactionDto> toDtoList(List<Transaction> transactions);

    /**
     * Convert DTO to entity
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "completedAt", ignore = true)
    Transaction toEntity(TransactionDto dto);
}