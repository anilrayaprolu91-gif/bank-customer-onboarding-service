package com.bank.onboarding.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Schema(description = "An individual risk factor contributing to a customer's overall risk score")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RiskFactorResponse {

    @Schema(description = "Unique factor identifier")
    private UUID id;

    @Schema(description = "Risk factor name", example = "POLITICALLY_EXPOSED_PERSON")
    private String factorName;

    @Schema(description = "Detailed description", example = "Customer is or has been a politically exposed person")
    private String factorDescription;

    @Schema(description = "Weight of this factor in the total risk score", example = "0.35")
    private BigDecimal weight;
}

