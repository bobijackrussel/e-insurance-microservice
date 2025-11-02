package com.einsurance.common.security;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * User principal object stored in SecurityContext
 * Contains authenticated user information extracted from JWT token
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserPrincipal implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private String userId;
    private String username;
    private String email;
    private List<String> roles;
    
    /**
     * Check if user has specific role
     */
    public boolean hasRole(String role) {
        return roles != null && roles.contains(role);
    }
    
    /**
     * Check if user is admin
     */
    public boolean isAdmin() {
        return hasRole("ADMIN");
    }
    
    /**
     * Check if user is customer
     */
    public boolean isCustomer() {
        return hasRole("CUSTOMER");
    }
}