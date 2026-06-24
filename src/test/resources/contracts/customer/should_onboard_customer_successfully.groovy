package contracts.customer

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description "Onboard customer returns 201 with enterprise response envelope"
    name "should_onboard_customer_successfully"

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
                        email                  : "contract.customer@example.com",
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
                        assessedBy : "AUTO_SCREENING_v2.1",
                        factors    : [[
                                factorName       : "GEOGRAPHIC_RISK",
                                factorDescription: "Standard-risk jurisdiction",
                                weight           : 0.1
                        ]]
                ],
                initialAccount: [
                        accountType   : "CHECKING",
                        currency      : "USD",
                        initialDeposit: 1000.00,
                        productCode   : "CHK-STANDARD"
                ]
        )
    }

    response {
        status CREATED()
        headers {
            contentType(applicationJson())
        }
        body(
                success  : true,
                message  : "Customer onboarded successfully",
                data     : [
                        id            : "00000000-0000-0000-0000-000000000000",
                        customerNumber: "CUST-0001-20241101",
                        email         : "contract.customer@example.com",
                        customerStatus: "PENDING_VERIFICATION"
                ],
                timestamp: "2026-01-01T10:30:00"
        )
        bodyMatchers {
            jsonPath('$.data.id', byRegex('[0-9a-fA-F\\-]{36}'))
            jsonPath('$.data.customerNumber', byRegex('CUST-.*'))
            jsonPath('$.timestamp', byRegex('^\\d{4}-\\d{2}-\\d{2}T.*$'))
        }
    }
}

