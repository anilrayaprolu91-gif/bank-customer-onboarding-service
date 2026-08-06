package com.bank.onboarding.authentication.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Login response DTO with JWT tokens.
 */
public record LoginResponse(
    @JsonProperty("access_token")
    String accessToken,

    @JsonProperty("refresh_token")
    String refreshToken,

    @JsonProperty("user_id")
    String userId,

    String username,
    String email,

    @JsonProperty("token_type")
    String tokenType
) {
    public LoginResponse(String accessToken, String refreshToken, String userId, String username, String email) {
        this(accessToken, refreshToken, userId, username, email, "Bearer");
    }
}

