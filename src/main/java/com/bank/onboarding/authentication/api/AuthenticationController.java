package com.bank.onboarding.authentication.api;

import com.bank.onboarding.authentication.api.dto.LoginRequest;
import com.bank.onboarding.authentication.api.dto.LoginResponse;
import com.bank.onboarding.authentication.api.dto.RefreshTokenRequest;
import com.bank.onboarding.authentication.application.AuthenticationService;
import com.bank.onboarding.shared.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Authentication REST controller for login and token management.
 * Provides JWT-based authentication for enterprise banking system.
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Authentication", description = "APIs for user authentication and token management")
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    /**
     * Login with username and password.
     * Returns access token and refresh token.
     */
    @PostMapping("/login")
    @Operation(summary = "User login", description = "Authenticate user and get JWT tokens")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        log.info("Login attempt for user: {}", request.username());

        AuthenticationService.AuthenticationResponse response = authenticationService.authenticate(
            request.username(),
            request.password()
        );

        LoginResponse loginResponse = new LoginResponse(
            response.accessToken(),
            response.refreshToken(),
            response.userId(),
            response.username(),
            response.email()
        );

        return ResponseEntity.ok(
            ApiResponse.success(loginResponse, "User authenticated successfully")
        );
    }

    /**
     * Refresh access token using valid refresh token.
     */
    @PostMapping("/refresh")
    @Operation(summary = "Refresh access token", description = "Get new access token using refresh token")
    public ResponseEntity<ApiResponse<LoginResponse>> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        AuthenticationService.AuthenticationResponse response = authenticationService.refreshAccessToken(
            request.refreshToken()
        );

        LoginResponse loginResponse = new LoginResponse(
            response.accessToken(),
            response.refreshToken(),
            response.userId(),
            response.username(),
            response.email()
        );

        return ResponseEntity.ok(
            ApiResponse.success(loginResponse, "Access token refreshed successfully")
        );
    }
}

