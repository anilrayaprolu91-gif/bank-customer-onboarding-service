package com.bank.onboarding.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Generic API response wrapper providing consistent envelope structure
 * for all endpoints in the onboarding service.
 */
@Schema(description = "Standard API response envelope")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    @Schema(description = "Indicates whether the request was successful", example = "true")
    private boolean success;

    @Schema(description = "Human-readable message describing the result", example = "Customer onboarded successfully")
    private String message;

    @Schema(description = "Response payload")
    private T data;

    @Schema(description = "Validation or business errors, present only when success=false")
    private List<String> errors;

    @Schema(description = "ISO-8601 timestamp of the response", example = "2024-11-01T10:30:00")
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();

    @Schema(description = "Trace identifier for distributed tracing", example = "abc123")
    private String traceId;

    public static <T> ApiResponse<T> success(T data, String message) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .build();
    }

    public static <T> ApiResponse<T> failure(String message, List<String> errors) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .errors(errors)
                .build();
    }
}

