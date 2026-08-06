package com.bank.onboarding.infrastructure.security;

import lombok.Getter;
import lombok.ToString;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

import java.util.List;

/**
 * Thread-safe security context holder for current user during request processing.
 */
@Component
@RequestScope
@Getter
@ToString
public class SecurityContextManager {

    private String userId;
    private List<String> roles;

    /**
     * Set security context for current request.
     */
    public void setSecurityContext(String userId, List<String> roles) {
        this.userId = userId;
        this.roles = roles;
    }

    /**
     * Check if current user has required role.
     */
    public boolean hasRole(String role) {
        return roles != null && roles.contains(role);
    }

    /**
     * Check if user is authenticated.
     */
    public boolean isAuthenticated() {
        return userId != null && roles != null;
    }

    /**
     * Get current user ID or throw exception if not authenticated.
     */
    public String getCurrentUserIdOrThrow() {
        if (userId == null) {
            throw new SecurityException("User not authenticated");
        }
        return userId;
    }
}

