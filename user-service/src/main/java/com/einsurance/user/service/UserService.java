package com.einsurance.user.service;

import com.einsurance.common.dto.PageResponse;
import com.einsurance.common.dto.UserDto;
import com.einsurance.common.dto.UserRegistrationRequest;
import com.einsurance.common.dto.UserUpdateRequest;
import com.einsurance.common.exception.ResourceAlreadyExistsException;
import com.einsurance.common.exception.ResourceNotFoundException;
import com.einsurance.common.exception.UnauthorizedException;
import com.einsurance.common.security.SecurityUtil;
import com.einsurance.user.entity.User;
import com.einsurance.user.mapper.UserMapper;
import com.einsurance.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Service layer for User operations
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    /**
     * Register a new user (called after Keycloak registration)
     */
    @Transactional
    public UserDto registerUser(UserRegistrationRequest request) {
        log.info("Registering new user with email: {}", request.getEmail());

        // Check if user already exists
        if (userRepository.existsByKeycloakId(request.getKeycloakId())) {
            throw new ResourceAlreadyExistsException("User", "keycloakId", request.getKeycloakId());
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ResourceAlreadyExistsException("User", "email", request.getEmail());
        }

        // Create new user
        User user = userMapper.toEntity(request);
        User savedUser = userRepository.save(user);

        log.info("User registered successfully with ID: {}", savedUser.getId());
        return userMapper.toDto(savedUser);
    }

    /**
     * Get user by ID
     */
    @Transactional(readOnly = true)
    public UserDto getUserById(UUID id) {
        log.debug("Fetching user by ID: {}", id);
        
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        
        return userMapper.toDto(user);
    }

    /**
     * Get user by Keycloak ID
     */
    @Transactional(readOnly = true)
    public UserDto getUserByKeycloakId(String keycloakId) {
        log.debug("Fetching user by Keycloak ID: {}", keycloakId);
        
        User user = userRepository.findByKeycloakId(keycloakId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "keycloakId", keycloakId));
        
        return userMapper.toDto(user);
    }

    /**
     * Get user by email
     */
    @Transactional(readOnly = true)
    public UserDto getUserByEmail(String email) {
        log.debug("Fetching user by email: {}", email);
        
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
        
        return userMapper.toDto(user);
    }

    /**
     * Get current logged-in user
     */
    @Transactional(readOnly = true)
    public UserDto getCurrentUser() {
        String keycloakId = SecurityUtil.getCurrentUserIdOrThrow();
        log.debug("Fetching current user with Keycloak ID: {}", keycloakId);
        
        return getUserByKeycloakId(keycloakId);
    }

    /**
     * Update current user profile
     */
    @Transactional
    public UserDto updateCurrentUser(UserUpdateRequest request) {
        String keycloakId = SecurityUtil.getCurrentUserIdOrThrow();
        log.info("Updating user profile for Keycloak ID: {}", keycloakId);

        User user = userRepository.findByKeycloakId(keycloakId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "keycloakId", keycloakId));

        // Update user fields
        userMapper.updateEntityFromDto(request, user);
        User updatedUser = userRepository.save(user);

        log.info("User profile updated successfully for ID: {}", updatedUser.getId());
        return userMapper.toDto(updatedUser);
    }

    /**
     * Update user by ID (admin only)
     */
    @Transactional
    public UserDto updateUserById(UUID id, UserUpdateRequest request) {
        log.info("Admin updating user with ID: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        userMapper.updateEntityFromDto(request, user);
        User updatedUser = userRepository.save(user);

        log.info("User updated successfully by admin: {}", updatedUser.getId());
        return userMapper.toDto(updatedUser);
    }

    /**
     * Get all users with pagination (admin only)
     */
    @Transactional(readOnly = true)
    public PageResponse<UserDto> getAllUsers(int page, int size, String sortBy, String sortDirection) {
        log.debug("Fetching all users - page: {}, size: {}", page, size);

        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<User> userPage = userRepository.findAll(pageable);
        
        return buildPageResponse(userPage);
    }

    /**
     * Get users by role (admin only)
     */
    @Transactional(readOnly = true)
    public PageResponse<UserDto> getUsersByRole(String role, int page, int size) {
        log.debug("Fetching users by role: {} - page: {}, size: {}", role, page, size);

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<User> userPage = userRepository.findByRole(role.toUpperCase(), pageable);
        
        return buildPageResponse(userPage);
    }

    /**
     * Search users by name or email (admin only)
     */
    @Transactional(readOnly = true)
    public PageResponse<UserDto> searchUsers(String searchTerm, int page, int size) {
        log.debug("Searching users with term: {} - page: {}, size: {}", searchTerm, page, size);

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<User> userPage = userRepository.searchUsers(searchTerm, pageable);
        
        return buildPageResponse(userPage);
    }

    /**
     * Deactivate user (soft delete - admin only)
     */
    @Transactional
    public void deactivateUser(UUID id) {
        log.info("Deactivating user with ID: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        // Prevent deactivating yourself
        String currentKeycloakId = SecurityUtil.getCurrentUserIdOrThrow();
        if (user.getKeycloakId().equals(currentKeycloakId)) {
            throw new UnauthorizedException("You cannot deactivate your own account");
        }

        user.setIsActive(false);
        userRepository.save(user);

        log.info("User deactivated successfully: {}", id);
    }

    /**
     * Activate user (admin only)
     */
    @Transactional
    public void activateUser(UUID id) {
        log.info("Activating user with ID: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        user.setIsActive(true);
        userRepository.save(user);

        log.info("User activated successfully: {}", id);
    }

    /**
     * Delete user permanently (admin only - use with caution)
     */
    @Transactional
    public void deleteUser(UUID id) {
        log.warn("Permanently deleting user with ID: {}", id);

        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("User", "id", id);
        }

        // Prevent deleting yourself
        String currentKeycloakId = SecurityUtil.getCurrentUserIdOrThrow();
        User user = userRepository.findById(id).get();
        if (user.getKeycloakId().equals(currentKeycloakId)) {
            throw new UnauthorizedException("You cannot delete your own account");
        }

        userRepository.deleteById(id);
        log.warn("User permanently deleted: {}", id);
    }

    /**
     * Get user statistics (admin only)
     */
    @Transactional(readOnly = true)
    public UserStatistics getUserStatistics() {
        log.debug("Fetching user statistics");

        long totalUsers = userRepository.count();
        long activeUsers = userRepository.countByIsActiveTrue();
        long adminUsers = userRepository.countByRole("ADMIN");
        long customerUsers = userRepository.countByRole("CUSTOMER");

        return UserStatistics.builder()
                .totalUsers(totalUsers)
                .activeUsers(activeUsers)
                .adminUsers(adminUsers)
                .customerUsers(customerUsers)
                .build();
    }

    /**
     * Helper method to build PageResponse
     */
    private PageResponse<UserDto> buildPageResponse(Page<User> userPage) {
        List<UserDto> userDtos = userMapper.toDtoList(userPage.getContent());
        
        return PageResponse.<UserDto>builder()
                .content(userDtos)
                .page(userPage.getNumber())
                .size(userPage.getSize())
                .totalElements(userPage.getTotalElements())
                .totalPages(userPage.getTotalPages())
                .isLast(userPage.isLast())
                .build();
    }
}