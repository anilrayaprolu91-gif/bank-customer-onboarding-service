package com.bank.onboarding.dto.request;

import com.bank.onboarding.domain.enums.DocumentType;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;

@Schema(description = "KYC document submitted for identity verification")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KycDocumentRequest {

    @Schema(description = "Type of identity document", example = "PASSPORT")
    @NotNull(message = "Document type is required")
    private DocumentType documentType;

    @Schema(description = "Unique document identifier number", example = "A12345678")
    @NotBlank(message = "Document number is required")
    @Size(max = 100, message = "Document number must not exceed 100 characters")
    private String documentNumber;

    @Schema(description = "Authority that issued the document", example = "U.S. Department of State")
    @Size(max = 200, message = "Issuing authority must not exceed 200 characters")
    private String issuingAuthority;

    @Schema(description = "ISO 3166-1 alpha-2 country that issued the document", example = "US")
    @NotBlank(message = "Issuing country is required")
    @Size(min = 2, max = 3, message = "Issuing country must be a 2 or 3 character ISO code")
    private String issuingCountry;

    @Schema(description = "Date the document was issued", example = "2015-06-01")
    @NotNull(message = "Issue date is required")
    @PastOrPresent(message = "Issue date must not be in the future")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate issueDate;

    @Schema(description = "Date the document expires (null for non-expiring documents)", example = "2025-06-01")
    @Future(message = "Expiry date must be in the future")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate expiryDate;
}

