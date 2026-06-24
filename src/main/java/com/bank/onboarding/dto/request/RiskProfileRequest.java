package com.bank.onboarding.dto.request;

import com.bank.onboarding.domain.enums.RiskLevel;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Schema(description = "Risk assessment profile for the customer")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RiskProfileRequest {

    @Schema(description = "Overall risk classification", example = "LOW")
    @NotNull(message = "Risk level is required")
    private RiskLevel riskLevel;

    @Schema(description = "Numeric risk score (0-100)", example = "25")
    @NotNull(message = "Risk score is required")
    @Min(value = 0, message = "Risk score must be between 0 and 100")
    @Max(value = 100, message = "Risk score must be between 0 and 100")
    private Integer riskScore;

    @Schema(description = "Agent or system that performed the assessment", example = "AUTO_SCREENING_v2.1")
    @Size(max = 100)
    private String assessedBy;

    @Schema(description = "Additional notes from the risk analyst", example = "Customer has clean financial history")
    @Size(max = 1000)
    private String assessmentNotes;

    @Schema(description = "Individual risk factors contributing to the overall score")
    @Valid
    @Builder.Default
    private List<RiskFactorRequest> factors = new ArrayList<>();
}

