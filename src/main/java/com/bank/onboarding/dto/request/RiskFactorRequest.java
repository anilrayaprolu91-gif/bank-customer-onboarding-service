package com.bank.onboarding.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

@Schema(description = "Individual risk factor in a customer risk profile")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RiskFactorRequest {

    @Schema(description = "Name of the risk factor", example = "POLITICALLY_EXPOSED_PERSON")
    @NotBlank(message = "Factor name is required")
    @Size(max = 100, message = "Factor name must not exceed 100 characters")
    private String factorName;

    @Schema(description = "Detailed description of the risk factor",
            example = "Customer is or has been a politically exposed person")
    @Size(max = 500, message = "Factor description must not exceed 500 characters")
    private String factorDescription;

    @Schema(description = "Weight of this factor in the overall risk score (0.0 - 1.0)", example = "0.35")
    @NotNull(message = "Factor weight is required")
    @DecimalMin(value = "0.0", message = "Weight must be between 0 and 1")
    @DecimalMax(value = "1.0", message = "Weight must be between 0 and 1")
    private BigDecimal weight;
}

