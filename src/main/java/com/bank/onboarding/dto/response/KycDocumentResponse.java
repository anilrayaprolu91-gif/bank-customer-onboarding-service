package com.bank.onboarding.dto.response;

import com.bank.onboarding.domain.enums.DocumentType;
import com.bank.onboarding.domain.enums.VerificationStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "KYC document associated with a customer")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class KycDocumentResponse {

    @Schema(description = "Unique document identifier")
    private UUID id;

    @Schema(description = "Type of document", example = "PASSPORT")
    private DocumentType documentType;

    @Schema(description = "Document number", example = "A12345678")
    private String documentNumber;

    @Schema(description = "Issuing authority", example = "U.S. Department of State")
    private String issuingAuthority;

    @Schema(description = "ISO country code of issuing country", example = "US")
    private String issuingCountry;

    @Schema(description = "Issue date", example = "2015-06-01")
    private LocalDate issueDate;

    @Schema(description = "Expiry date", example = "2025-06-01")
    private LocalDate expiryDate;

    @Schema(description = "Current verification status", example = "VERIFIED")
    private VerificationStatus verificationStatus;

    @Schema(description = "Agent or system that verified the document", example = "compliance-team@bank.com")
    private String verifiedBy;

    @Schema(description = "Timestamp when the document was verified")
    private LocalDateTime verifiedAt;

    @Schema(description = "Reason if document was rejected")
    private String rejectionReason;

    @Schema(description = "Record creation timestamp")
    private LocalDateTime createdAt;
}

