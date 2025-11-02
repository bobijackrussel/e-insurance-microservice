package com.einsurance.common.security;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.RSAPublicKeySpec;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;

/**
 * Utility class for JWT token operations
 * Validates Keycloak RS256 JWT tokens using public key from JWKS endpoint
 */
@Slf4j
@Component
public class JwtUtil {

    @Value("${keycloak.auth-server-url:http://localhost:9098}")
    private String keycloakUrl;

    @Value("${keycloak.realm:todoapp-realm}")
    private String realm;

    private PublicKey cachedPublicKey;
    private LocalDateTime lastKeyFetch;
    private static final int KEY_CACHE_HOURS = 24;

    private RestTemplate restTemplate;
    private ObjectMapper objectMapper;

    // Initialize in constructor to avoid field injection issues
    public JwtUtil() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Get Keycloak public key for token validation
     * Fetches from JWKS endpoint and caches for 24 hours
     */
    private PublicKey getPublicKey() {
        if (cachedPublicKey != null && lastKeyFetch != null) {
            if (lastKeyFetch.plusHours(KEY_CACHE_HOURS).isAfter(LocalDateTime.now())) {
                log.debug("Using cached public key");
                return cachedPublicKey;
            }
        }

        log.info("Fetching public key from Keycloak JWKS endpoint");
        try {
            String jwksUrl = keycloakUrl + "/realms/" + realm + "/protocol/openid-connect/certs";
            log.debug("JWKS URL: {}", jwksUrl);

            String jwksResponse = restTemplate.getForObject(jwksUrl, String.class);
            JsonNode jwks = objectMapper.readTree(jwksResponse);
            JsonNode keys = jwks.get("keys");

            if (keys != null && keys.isArray() && keys.size() > 0) {
                // Get the first key (Keycloak typically has one active signing key)
                JsonNode key = keys.get(0);
                String n = key.get("n").asText(); // Modulus
                String e = key.get("e").asText(); // Exponent

                // Decode Base64URL encoded values
                byte[] nBytes = Base64.getUrlDecoder().decode(n);
                byte[] eBytes = Base64.getUrlDecoder().decode(e);

                // Create RSA public key
                BigInteger modulus = new BigInteger(1, nBytes);
                BigInteger exponent = new BigInteger(1, eBytes);
                RSAPublicKeySpec spec = new RSAPublicKeySpec(modulus, exponent);
                KeyFactory factory = KeyFactory.getInstance("RSA");

                cachedPublicKey = factory.generatePublic(spec);
                lastKeyFetch = LocalDateTime.now();

                log.info("Successfully fetched and cached Keycloak public key");
                return cachedPublicKey;
            }

            throw new RuntimeException("No keys found in JWKS endpoint");

        } catch (Exception e) {
            log.error("Failed to fetch public key from Keycloak JWKS endpoint", e);
            throw new RuntimeException("Cannot validate JWT token - failed to fetch public key", e);
        }
    }

    /**
     * Extract username from token
     * Keycloak uses 'preferred_username' claim
     */
    public String extractUsername(String token) {
        Claims claims = extractAllClaims(token);
        String preferredUsername = claims.get("preferred_username", String.class);
        if (preferredUsername != null) {
            return preferredUsername;
        }
        // Fallback to subject if preferred_username not present
        return claims.getSubject();
    }

    /**
     * Extract user ID from token
     */
    public String extractUserId(String token) {
        Claims claims = extractAllClaims(token);
        return claims.get("sub", String.class);
    }

    /**
     * Extract email from token
     */
    public String extractEmail(String token) {
        Claims claims = extractAllClaims(token);
        return claims.get("email", String.class);
    }

    /**
     * Extract roles from token
     * Keycloak stores roles in realm_access.roles
     */
    @SuppressWarnings("unchecked")
    public List<String> extractRoles(String token) {
        Claims claims = extractAllClaims(token);

        // Try realm_access.roles first (Keycloak standard structure)
        Map<String, Object> realmAccess = claims.get("realm_access", Map.class);
        if (realmAccess != null && realmAccess.containsKey("roles")) {
            List<String> roles = (List<String>) realmAccess.get("roles");
            if (roles != null && !roles.isEmpty()) {
                log.debug("Extracted roles from realm_access: {}", roles);
                return roles;
            }
        }

        // Fallback to direct 'roles' claim (for backward compatibility)
        List<String> directRoles = claims.get("roles", List.class);
        if (directRoles != null) {
            log.debug("Extracted roles from direct claim: {}", directRoles);
            return directRoles;
        }

        log.warn("No roles found in token");
        return new ArrayList<>();
    }

    /**
     * Extract Keycloak user ID from token
     */
    public String extractKeycloakId(String token) {
        Claims claims = extractAllClaims(token);
        return claims.get("sub", String.class); // Keycloak uses 'sub' for user ID
    }

    /**
     * Extract expiration date from token
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Extract specific claim from token
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Extract all claims from token
     * Validates Keycloak RS256 token with public key
     */
    public Claims extractAllClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(getPublicKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (Exception e) {
            log.error("Failed to parse JWT token", e);
            throw new RuntimeException("Invalid JWT token", e);
        }
    }

    /**
     * Check if token is expired
     */
    public Boolean isTokenExpired(String token) {
        try {
            Date expiration = extractExpiration(token);
            return expiration.before(new Date());
        } catch (Exception e) {
            log.error("Failed to check token expiration", e);
            return true;
        }
    }

    /**
     * Validate token
     */
    public Boolean validateToken(String token) {
        try {
            extractAllClaims(token);
            return !isTokenExpired(token);
        } catch (Exception e) {
            log.error("Token validation failed", e);
            return false;
        }
    }

    /**
     * Validate token against username
     */
    public Boolean validateToken(String token, String username) {
        try {
            final String tokenUsername = extractUsername(token);
            return (tokenUsername.equals(username) && !isTokenExpired(token));
        } catch (Exception e) {
            log.error("Token validation failed for user: {}", username, e);
            return false;
        }
    }

    /**
     * Check if user has specific role
     */
    public Boolean hasRole(String token, String role) {
        try {
            List<String> roles = extractRoles(token);
            return roles != null && roles.contains(role);
        } catch (Exception e) {
            log.error("Failed to check role", e);
            return false;
        }
    }

    /**
     * Check if user is admin
     */
    public Boolean isAdmin(String token) {
        return hasRole(token, "ADMIN");
    }

    /**
     * Check if user is customer
     */
    public Boolean isCustomer(String token) {
        return hasRole(token, "CUSTOMER");
    }

    /**
     * Extract token from Bearer header
     */
    public String extractTokenFromHeader(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }
}