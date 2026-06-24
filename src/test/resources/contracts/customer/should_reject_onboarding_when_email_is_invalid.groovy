package contracts.customer

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description "Onboard customer returns 400 RFC7807 when email format is invalid"
    name "should_reject_onboarding_when_email_is_invalid"

    request {
        method POST()
        url "/api/v1/customers/onboard"
        headers {
            contentType(applicationJson())
            accept(applicationJson())
        }
        body(
                personalInfo: [
                        firstName              : "Jane",
                        lastName               : "Smith",
                        dateOfBirth            : "1992-04-18",
                        nationality            : "US",
                        taxIdentificationNumber: "987-65-1234",
                        email                  : "not-an-email",
                        phoneNumber            : "+15551234567"
                ],
                addresses   : [[
                        addressType: "HOME",
                        street     : "789 Elm Street",
                        city       : "Chicago",
                        state      : "IL",
                        postalCode : "60601",
                        country    : "US",
                        isPrimary  : true
                ]],
                kycDocuments: [[
                        documentType    : "PASSPORT",
                        documentNumber  : "P12345678",
                        issuingAuthority: "U.S. Department of State",
                        issuingCountry  : "US",
                        issueDate       : "2018-01-15",
                        expiryDate      : "2030-01-15"
                ]],
                riskProfile : [
                        riskLevel  : "LOW",
                        riskScore  : 20,
                        assessedBy : "AUTO_SCREENING_v2.1"
                ]
        )
    }

    response {
        status BAD_REQUEST()
        headers {
            contentType("application/problem+json")
        }
        body(
                title    : "Validation Failed",
                detail   : "Validation Failed",
                status   : 400,
                errors   : ["personalInfo.email: Email must be a valid email address"],
                timestamp: "2026-01-01T10:30:00Z"
        )
        bodyMatchers {
            jsonPath('$.timestamp', byRegex('^\\d{4}-\\d{2}-\\d{2}T.*$'))
            jsonPath('$.errors[0]', byRegex('personalInfo\\.email: .*'))
        }
    }
}


