package com.einsurance.user.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * User entity representing user profile information
 * Synced with Keycloak for authentication
 */
@Entity
@Table(name = "users", indexes = {
    @Index(name = "idx_keycloak_id", columnList = "keycloak_id"),
    @Index(name = "idx_email", columnList = "email"),
    @Index(name = "idx_role", columnList = "role")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(columnDefinition = "UUID")
    private UUID id;

    @Column(name = "keycloak_id", unique = true, nullable = false)
    private String keycloakId;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(name = "first_name", length = 100)
    private String firstName;

    @Column(name = "last_name", length = 100)
    private String lastName;

    @Column(length = 20)
    private String phone;

    @Column(columnDefinition = "TEXT")
    private String address;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(nullable = false, length = 20)
    @Builder.Default
    private String role = "CUSTOMER";

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Get full name
     */
    public String getFullName() {
        return (firstName != null ? firstName : "") + " " + (lastName != null ? lastName : "");
    }

    /**
     * Check if user is admin
     */
    public boolean isAdmin() {
        return "ADMIN".equals(role);
    }

    /**
     * Check if user is customer
     */
    public boolean isCustomer() {
        return "CUSTOMER".equals(role);
    }

    @PrePersist
    public void prePersist() {
        if (role == null) {
            role = "CUSTOMER";
        }
        if (isActive == null) {
            isActive = true;
        }
    }
}