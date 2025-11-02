package com.einsurance.common.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * JWT Authentication Filter
 * Intercepts requests, validates JWT tokens from Authorization header,
 * and sets authentication in SecurityContext
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        try {
            // Extract JWT token from Authorization header
            String authHeader = request.getHeader("Authorization");
            
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                log.debug("No Bearer token found in request to: {}", request.getRequestURI());
                filterChain.doFilter(request, response);
                return;
            }

            String token = authHeader.substring(7);
            
            // Validate token
            if (!jwtUtil.validateToken(token)) {
                log.warn("Invalid JWT token for request to: {}", request.getRequestURI());
                filterChain.doFilter(request, response);
                return;
            }

            // Extract user details from token
            String username = jwtUtil.extractUsername(token);
            String userId = jwtUtil.extractUserId(token);
            String email = jwtUtil.extractEmail(token);
            List<String> roles = jwtUtil.extractRoles(token);

            log.debug("Authenticated user: {} (ID: {}, Email: {}, Roles: {})", 
                    username, userId, email, roles);

            // Convert roles to Spring Security authorities
            // Handle both cases: roles with and without "ROLE_" prefix
            List<SimpleGrantedAuthority> authorities = roles.stream()
                    .map(role -> {
                        if (role.startsWith("ROLE_")) {
                            return new SimpleGrantedAuthority(role);
                        } else {
                            return new SimpleGrantedAuthority("ROLE_" + role);
                        }
                    })
                    .collect(Collectors.toList());

            // Create authentication object
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(username, null, authorities);
            
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            // Store user details in authentication for later use
            UserPrincipal userPrincipal = UserPrincipal.builder()
                    .userId(userId)
                    .username(username)
                    .email(email)
                    .roles(roles)
                    .build();

            authentication = new UsernamePasswordAuthenticationToken(
                    userPrincipal, null, authorities);

            // Set authentication in SecurityContext
            SecurityContextHolder.getContext().setAuthentication(authentication);

            log.debug("Successfully authenticated user: {}", username);

        } catch (Exception e) {
            log.error("Failed to authenticate user from JWT token", e);
            // Don't throw exception, let the request continue
            // Spring Security will handle unauthorized access
        }

        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        // Skip filter for public endpoints
        String path = request.getRequestURI();
        return path.startsWith("/actuator") || 
               path.startsWith("/swagger-ui") || 
               path.startsWith("/v3/api-docs") ||
               path.startsWith("/error");
    }
}