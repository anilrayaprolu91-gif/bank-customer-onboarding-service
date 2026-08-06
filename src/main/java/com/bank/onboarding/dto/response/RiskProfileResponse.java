package com.bank.onboarding.dto.response;

import com.bank.onboarding.risk.domain.RiskLevel;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Schema(description = "Customer risk profile assessed during onboarding")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RiskProfileResponse {

    @Schema(description = "Unique risk profile identifier")
    private UUID id;

    @Schema(description = "Overall risk level classification", example = "LOW")
    private RiskLevel riskLevel;

    @Schema(description = "Numeric risk score (0-100)", example = "25")
    private Integer riskScore;

    @Schema(description = "Agent or system that performed the assessment", example = "AUTO_SCREENING_v2.1")
    private String assessedBy;

    @Schema(description = "Assessment timestamp")
    private LocalDateTime assessedAt;

    @Schema(description = "Scheduled next review date")
    private LocalDate nextReviewDate;

    @Schema(description = "Assessment notes from the risk analyst")
    private String assessmentNotes;

    @Schema(description = "Individual risk factors")
    private List<RiskFactorResponse> riskFactors;

    @Schema(description = "Record creation timestamp")
    private LocalDateTime createdAt;

    @Schema(description = "Record last-updated timestamp")
    private LocalDateTime updatedAt;
}


