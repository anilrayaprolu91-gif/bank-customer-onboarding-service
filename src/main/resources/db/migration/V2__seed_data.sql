-- ============================================================
-- V2 - Seed data for development / demo purposes
-- ============================================================

-- ── Customer 1 – Active, Low Risk ────────────────────────────────────────────
INSERT INTO customers (id, customer_number, first_name, last_name, email, phone_number,
                       date_of_birth, nationality, tax_identification_number,
                       customer_status, onboarding_date, created_at, updated_at)
VALUES ('a1b2c3d4-e5f6-7890-abcd-ef1234567890',
        'CUST-0001-20241101', 'Alice', 'Johnson', 'alice.johnson@example.com',
        '+15550101010', '1985-03-22', 'US', '111-22-3333',
        'ACTIVE', '2024-11-01 09:00:00', NOW(), NOW());

INSERT INTO addresses (customer_id, address_type, street, city, state, postal_code, country, is_primary)
VALUES ('a1b2c3d4-e5f6-7890-abcd-ef1234567890', 'HOME', '456 Oak Avenue', 'San Francisco', 'CA', '94102', 'US', TRUE),
       ('a1b2c3d4-e5f6-7890-abcd-ef1234567890', 'WORK', '1 Market Street', 'San Francisco', 'CA', '94105', 'US', FALSE);

INSERT INTO kyc_documents (customer_id, document_type, document_number, issuing_authority,
                           issuing_country, issue_date, expiry_date, verification_status, verified_by, verified_at)
VALUES ('a1b2c3d4-e5f6-7890-abcd-ef1234567890', 'PASSPORT', 'US98765432',
        'U.S. Department of State', 'US', '2019-08-15', '2029-08-15',
        'VERIFIED', 'compliance-bot@bank.com', '2024-11-01 10:00:00'),
       ('a1b2c3d4-e5f6-7890-abcd-ef1234567890', 'DRIVERS_LICENSE', 'CA-DL-12345678',
        'California DMV', 'US', '2021-01-10', '2027-01-10',
        'VERIFIED', 'compliance-bot@bank.com', '2024-11-01 10:05:00');

INSERT INTO risk_profiles (id, customer_id, risk_level, risk_score, assessed_at, assessed_by, next_review_date)
VALUES ('b2c3d4e5-f6a7-8901-bcde-f12345678901',
        'a1b2c3d4-e5f6-7890-abcd-ef1234567890',
        'LOW', 18, '2024-11-01 10:10:00', 'AUTO_SCREENING_v2.1', '2025-11-01');

INSERT INTO risk_factors (risk_profile_id, factor_name, factor_description, weight)
VALUES ('b2c3d4e5-f6a7-8901-bcde-f12345678901', 'GEOGRAPHIC_RISK',
        'Customer resides in a standard-risk jurisdiction', 0.10),
       ('b2c3d4e5-f6a7-8901-bcde-f12345678901', 'TRANSACTION_HISTORY',
        'No adverse transaction history detected', 0.08);

INSERT INTO accounts (account_number, customer_id, account_type, currency, balance,
                      account_status, opened_date, product_code)
VALUES ('ACC-0001-8734561', 'a1b2c3d4-e5f6-7890-abcd-ef1234567890',
        'CHECKING', 'USD', 15250.0000, 'ACTIVE', '2024-11-01 11:00:00', 'CHK-PREMIER'),
       ('ACC-0001-8734562', 'a1b2c3d4-e5f6-7890-abcd-ef1234567890',
        'SAVINGS', 'USD', 50000.0000, 'ACTIVE', '2024-11-01 11:05:00', 'SAV-STANDARD');

-- ── Customer 2 – Under Review, Medium Risk ────────────────────────────────────
INSERT INTO customers (id, customer_number, first_name, last_name, email, phone_number,
                       date_of_birth, nationality, tax_identification_number,
                       customer_status, onboarding_date, created_at, updated_at)
VALUES ('c3d4e5f6-a7b8-9012-cdef-123456789012',
        'CUST-0002-20241105', 'Bob', 'Martinez', 'bob.martinez@example.com',
        '+442071234567', '1978-07-11', 'GB', NULL,
        'UNDER_REVIEW', '2024-11-05 14:00:00', NOW(), NOW());

INSERT INTO addresses (customer_id, address_type, street, city, state, postal_code, country, is_primary)
VALUES ('c3d4e5f6-a7b8-9012-cdef-123456789012', 'HOME', '10 Downing Street', 'London', NULL, 'SW1A 2AA', 'GB', TRUE);

INSERT INTO kyc_documents (customer_id, document_type, document_number, issuing_authority,
                           issuing_country, issue_date, expiry_date, verification_status)
VALUES ('c3d4e5f6-a7b8-9012-cdef-123456789012', 'NATIONAL_ID', 'GB-NI-AB123456C',
        'HM Passport Office', 'GB', '2020-03-01', '2030-03-01', 'IN_REVIEW');

INSERT INTO risk_profiles (id, customer_id, risk_level, risk_score, assessed_at, assessed_by, next_review_date,
                           assessment_notes)
VALUES ('d4e5f6a7-b8c9-0123-defa-234567890123',
        'c3d4e5f6-a7b8-9012-cdef-123456789012',
        'MEDIUM', 52, '2024-11-05 15:00:00', 'AUTO_SCREENING_v2.1', '2025-05-05',
        'Manual review triggered due to cross-border transaction patterns');

INSERT INTO risk_factors (risk_profile_id, factor_name, factor_description, weight)
VALUES ('d4e5f6a7-b8c9-0123-defa-234567890123', 'CROSS_BORDER_ACTIVITY',
        'Customer has frequent cross-border financial activity', 0.35),
       ('d4e5f6a7-b8c9-0123-defa-234567890123', 'INDUSTRY_EXPOSURE',
        'Customer works in a higher-scrutiny industry', 0.17);

