package com.einsurance.user.repository;

import com.einsurance.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for User entity operations
 */
@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    /**
     * Find user by Keycloak ID
     */
    Optional<User> findByKeycloakId(String keycloakId);

    /**
     * Find user by email
     */
    Optional<User> findByEmail(String email);

    /**
     * Check if user exists by Keycloak ID
     */
    boolean existsByKeycloakId(String keycloakId);

    /**
     * Check if user exists by email
     */
    boolean existsByEmail(String email);

    /**
     * Find all active users
     */
    List<User> findByIsActiveTrue();

    /**
     * Find users by role
     */
    List<User> findByRole(String role);

    /**
     * Find users by role with pagination
     */
    Page<User> findByRole(String role, Pageable pageable);

    /**
     * Find active users by role
     */
    List<User> findByRoleAndIsActiveTrue(String role);

    /**
     * Search users by name or email
     */
    @Query("SELECT u FROM User u WHERE " +
           "LOWER(u.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(u.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<User> searchUsers(@Param("searchTerm") String searchTerm, Pageable pageable);

    /**
     * Find all users with pagination
     */
    Page<User> findAll(Pageable pageable);

    /**
     * Count users by role
     */
    long countByRole(String role);

    /**
     * Count active users
     */
    long countByIsActiveTrue();
}