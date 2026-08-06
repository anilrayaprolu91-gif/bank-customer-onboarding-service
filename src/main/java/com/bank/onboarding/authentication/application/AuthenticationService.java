package com.bank.onboarding.authentication.application;

import com.bank.onboarding.authentication.domain.BankUser;
import com.bank.onboarding.authentication.domain.UserStatus;
import com.bank.onboarding.authentication.repository.BankUserRepository;
import com.bank.onboarding.infrastructure.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Authentication service for login, token generation, and password management.
 * Implements industry-standard security practices for banking systems.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class AuthenticationService {

    private final BankUserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;

    private static final int MAX_FAILED_ATTEMPTS = 5;

    /**
     * Authenticate user with username and password.
     * Returns JWT access token and refresh token on success.
     */
    @Transactional
    public AuthenticationResponse authenticate(String username, String password) {
        BankUser user = userRepository.findByUsernameWithRoles(username)
            .orElseThrow(() -> new AuthenticationException("Invalid username or password"));

        if (!user.isActive()) {
            throw new AuthenticationException("User account is not active");
        }

        if (user.isLocked()) {
            throw new AuthenticationException("User account is locked. Contact administrator.");
        }

        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            handleFailedLogin(user);
            throw new AuthenticationException("Invalid username or password");
        }

        // Reset failed attempts on successful login
        user.setFailedLoginAttempts(0);
        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);

        // Generate tokens
        List<String> roles = user.getRoles().stream()
            .map(role -> "ROLE_" + role.getName())
            .collect(Collectors.toList());

        String accessToken = jwtTokenProvider.generateAccessToken(
            user.getId().toString(),
            user.getUsername(),
            roles
        );

        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getId().toString());

        log.info("User {} authenticated successfully", username);

        return new AuthenticationResponse(
            accessToken,
            refreshToken,
            user.getId().toString(),
            user.getUsername(),
            user.getEmail()
        );
    }

    /**
     * Refresh access token using valid refresh token.
     */
    @Transactional
    public AuthenticationResponse refreshAccessToken(String refreshToken) {
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new AuthenticationException("Invalid refresh token");
        }

        String userId = jwtTokenProvider.extractUserId(refreshToken);
        BankUser user = userRepository.findById(java.util.UUID.fromString(userId))
            .orElseThrow(() -> new AuthenticationException("User not found"));

        List<String> roles = user.getRoles().stream()
            .map(role -> "ROLE_" + role.getName())
            .collect(Collectors.toList());

        String newAccessToken = jwtTokenProvider.generateAccessToken(
            user.getId().toString(),
            user.getUsername(),
            roles
        );

        return new AuthenticationResponse(
            newAccessToken,
            refreshToken,
            user.getId().toString(),
            user.getUsername(),
            user.getEmail()
        );
    }

    /**
     * Handle failed login attempt with account lockout after max attempts.
     */
    @Transactional
    public void handleFailedLogin(BankUser user) {
        user.setFailedLoginAttempts(user.getFailedLoginAttempts() + 1);

        if (user.getFailedLoginAttempts() >= MAX_FAILED_ATTEMPTS) {
            user.setAccountLocked(true);
            user.setStatus(UserStatus.LOCKED);
            log.warn("User {} locked after {} failed login attempts", user.getUsername(), MAX_FAILED_ATTEMPTS);
        }

        userRepository.save(user);
    }

    /**
     * Unlock user account (admin operation).
     */
    @Transactional
    public void unlockUserAccount(java.util.UUID userId) {
        BankUser user = userRepository.findById(userId)
            .orElseThrow(() -> new AuthenticationException("User not found"));

        user.setAccountLocked(false);
        user.setFailedLoginAttempts(0);
        user.setStatus(UserStatus.ACTIVE);

        userRepository.save(user);
        log.info("User {} account unlocked", user.getUsername());
    }

    /**
     * DTOs and exceptions.
     */
    public record AuthenticationResponse(
        String accessToken,
        String refreshToken,
        String userId,
        String username,
        String email
    ) {}

    public static class AuthenticationException extends RuntimeException {
        public AuthenticationException(String message) {
            super(message);
        }
    }
}

