package com.einsurance.common.security;

import com.einsurance.common.exception.UnauthenticatedException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Security utility class for accessing current user information
 * Provides convenient methods to get authenticated user details
 */
@Slf4j
@Component
public class SecurityUtil {

    /**
     * Get current authenticated user
     */
    public static Optional<UserPrincipal> getCurrentUser() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            
            if (authentication == null || !authentication.isAuthenticated()) {
                return Optional.empty();
            }
            
            Object principal = authentication.getPrincipal();
            
            if (principal instanceof UserPrincipal) {
                return Optional.of((UserPrincipal) principal);
            }
            
            return Optional.empty();
        } catch (Exception e) {
            log.error("Error getting current user", e);
            return Optional.empty();
        }
    }

    /**
     * Get current authenticated user or throw exception
     */
    public static UserPrincipal getCurrentUserOrThrow() {
        return getCurrentUser()
                .orElseThrow(() -> new UnauthenticatedException("User not authenticated"));
    }

    /**
     * Get current user ID
     */
    public static Optional<String> getCurrentUserId() {
        return getCurrentUser().map(UserPrincipal::getUserId);
    }

    /**
     * Get current user ID or throw exception
     */
    public static String getCurrentUserIdOrThrow() {
        return getCurrentUserId()
                .orElseThrow(() -> new UnauthenticatedException("User not authenticated"));
    }

    /**
     * Get current username
     */
    public static Optional<String> getCurrentUsername() {
        return getCurrentUser().map(UserPrincipal::getUsername);
    }

    /**
     * Get current user email
     */
    public static Optional<String> getCurrentUserEmail() {
        return getCurrentUser().map(UserPrincipal::getEmail);
    }

    /**
     * Check if current user has specific role
     */
    public static boolean hasRole(String role) {
        return getCurrentUser()
                .map(user -> user.hasRole(role))
                .orElse(false);
    }

    /**
     * Check if current user is admin
     */
    public static boolean isAdmin() {
        return hasRole("ADMIN");
    }

    /**
     * Check if current user is customer
     */
    public static boolean isCustomer() {
        return hasRole("CUSTOMER");
    }

    /**
     * Check if current user is the owner of a resource
     */
    public static boolean isOwner(String resourceUserId) {
        return getCurrentUserId()
                .map(currentUserId -> currentUserId.equals(resourceUserId))
                .orElse(false);
    }

    /**
     * Check if current user is admin or owner of resource
     */
    public static boolean isAdminOrOwner(String resourceUserId) {
        return isAdmin() || isOwner(resourceUserId);
    }
}