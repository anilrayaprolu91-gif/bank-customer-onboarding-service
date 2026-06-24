package com.bank.onboarding.dto.request;

import com.bank.onboarding.domain.enums.CustomerStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;

@Schema(description = "Request to update a customer's onboarding status")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerStatusUpdateRequest {

    @Schema(description = "New customer status", example = "ACTIVE")
    @NotNull(message = "Status is required")
    private CustomerStatus status;

    @Schema(description = "Reason for the status change", example = "All KYC documents verified successfully")
    @NotBlank(message = "Status reason is required")
    @Size(max = 500, message = "Status reason must not exceed 500 characters")
    private String reason;
}

