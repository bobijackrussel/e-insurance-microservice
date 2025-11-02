package com.einsurance.user.controller;

import com.einsurance.common.dto.ApiResponse;
import com.einsurance.common.dto.PageResponse;
import com.einsurance.common.dto.UserDto;
import com.einsurance.common.dto.UserRegistrationRequest;
import com.einsurance.common.dto.UserUpdateRequest;
import com.einsurance.user.service.UserService;
import com.einsurance.user.service.UserStatistics;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * REST controller for User operations
 */
@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "User Management", description = "APIs for user profile management")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Register new user", description = "Create user profile after Keycloak registration")
    public ApiResponse<UserDto> registerUser(@Valid @RequestBody UserRegistrationRequest request) {
        log.info("Received user registration request for email: {}", request.getEmail());
        UserDto user = userService.registerUser(request);
        return ApiResponse.success("User registered successfully", user);
    }

    @GetMapping("/me")
    @Operation(summary = "Get current user", description = "Get logged-in user profile")
    public ApiResponse<UserDto> getCurrentUser() {
        log.info("Fetching current user profile");
        UserDto user = userService.getCurrentUser();
        return ApiResponse.success(user);
    }

    @PutMapping("/me")
    @Operation(summary = "Update current user", description = "Update logged-in user profile")
    public ApiResponse<UserDto> updateCurrentUser(@Valid @RequestBody UserUpdateRequest request) {
        log.info("Updating current user profile");
        UserDto user = userService.updateCurrentUser(request);
        return ApiResponse.success("Profile updated successfully", user);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get user by ID", description = "Get user profile by ID (Admin only)")
    public ApiResponse<UserDto> getUserById(@PathVariable UUID id) {
        log.info("Fetching user by ID: {}", id);
        UserDto user = userService.getUserById(id);
        return ApiResponse.success(user);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update user by ID", description = "Update user profile by ID (Admin only)")
    public ApiResponse<UserDto> updateUserById(
            @PathVariable UUID id,
            @Valid @RequestBody UserUpdateRequest request) {
        log.info("Admin updating user: {}", id);
        UserDto user = userService.updateUserById(id, request);
        return ApiResponse.success("User updated successfully", user);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all users", description = "Get paginated list of all users (Admin only)")
    public ApiResponse<PageResponse<UserDto>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection) {
        log.info("Fetching all users - page: {}, size: {}", page, size);
        PageResponse<UserDto> users = userService.getAllUsers(page, size, sortBy, sortDirection);
        return ApiResponse.success(users);
    }

    @GetMapping("/role/{role}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get users by role", description = "Get users filtered by role (Admin only)")
    public ApiResponse<PageResponse<UserDto>> getUsersByRole(
            @PathVariable String role,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("Fetching users by role: {}", role);
        PageResponse<UserDto> users = userService.getUsersByRole(role, page, size);
        return ApiResponse.success(users);
    }

    @GetMapping("/search")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Search users", description = "Search users by name or email (Admin only)")
    public ApiResponse<PageResponse<UserDto>> searchUsers(
            @RequestParam String term,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("Searching users with term: {}", term);
        PageResponse<UserDto> users = userService.searchUsers(term, page, size);
        return ApiResponse.success(users);
    }

    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Deactivate user", description = "Soft delete user (Admin only)")
    public ApiResponse<Void> deactivateUser(@PathVariable UUID id) {
        log.info("Deactivating user: {}", id);
        userService.deactivateUser(id);
        return ApiResponse.success("User deactivated successfully");
    }

    @PatchMapping("/{id}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Activate user", description = "Reactivate deactivated user (Admin only)")
    public ApiResponse<Void> activateUser(@PathVariable UUID id) {
        log.info("Activating user: {}", id);
        userService.activateUser(id);
        return ApiResponse.success("User activated successfully");
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete user", description = "Permanently delete user (Admin only)")
    public ApiResponse<Void> deleteUser(@PathVariable UUID id) {
        log.info("Deleting user: {}", id);
        userService.deleteUser(id);
        return ApiResponse.success("User deleted successfully");
    }

    @GetMapping("/statistics")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get user statistics", description = "Get user count statistics (Admin only)")
    public ApiResponse<Object> getUserStatistics() {
        log.info("Fetching user statistics");
        Object stats = userService.getUserStatistics();
        return ApiResponse.success(stats);
    }

    @GetMapping("/keycloak/{keycloakId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get user by Keycloak ID", description = "Find user by Keycloak ID (Admin only)")
    public ApiResponse<UserDto> getUserByKeycloakId(@PathVariable String keycloakId) {
        log.info("Fetching user by Keycloak ID: {}", keycloakId);
        UserDto user = userService.getUserByKeycloakId(keycloakId);
        return ApiResponse.success(user);
    }

    @GetMapping("/email/{email}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get user by email", description = "Find user by email address (Admin only)")
    public ApiResponse<UserDto> getUserByEmail(@PathVariable String email) {
        log.info("Fetching user by email: {}", email);
        UserDto user = userService.getUserByEmail(email);
        return ApiResponse.success(user);
    }
}