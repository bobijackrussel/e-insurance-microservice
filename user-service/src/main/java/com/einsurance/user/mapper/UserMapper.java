package com.einsurance.user.mapper;

import com.einsurance.common.dto.UserDto;
import com.einsurance.common.dto.UserRegistrationRequest;
import com.einsurance.common.dto.UserUpdateRequest;
import com.einsurance.user.entity.User;
import org.mapstruct.*;

import java.util.List;

/**
 * MapStruct mapper for User entity and DTOs
 * Automatically generates implementation at compile time
 */
@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface UserMapper {

    /**
     * Convert User entity to UserDto
     */
    UserDto toDto(User user);

    /**
     * Convert list of User entities to list of UserDtos
     */
    List<UserDto> toDtoList(List<User> users);

    /**
     * Convert UserRegistrationRequest to User entity
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "isActive", constant = "true")
    @Mapping(target = "role", constant = "CUSTOMER")
    User toEntity(UserRegistrationRequest request);

    /**
     * Update User entity from UserUpdateRequest
     * Only updates non-null fields
     */
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "keycloakId", ignore = true)
    @Mapping(target = "email", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromDto(UserUpdateRequest request, @MappingTarget User user);
}