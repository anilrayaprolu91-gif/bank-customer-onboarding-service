-- ============================================================
-- V1 - Initial Schema for Bank Customer Onboarding Service
-- ============================================================

-- Enable UUID generation
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- ── customers ────────────────────────────────────────────────────────────────
CREATE TABLE customers
(
    id                        UUID         NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY,
    customer_number           VARCHAR(20)  NOT NULL,
    first_name                VARCHAR(100) NOT NULL,
    last_name                 VARCHAR(100) NOT NULL,
    email                     VARCHAR(255) NOT NULL,
    phone_number              VARCHAR(20),
    date_of_birth             DATE         NOT NULL,
    nationality               VARCHAR(3)   NOT NULL,
    tax_identification_number VARCHAR(50),
    customer_status           VARCHAR(30)  NOT NULL DEFAULT 'PENDING_VERIFICATION',
    status_reason             VARCHAR(500),
    onboarding_date           TIMESTAMP    NOT NULL,
    created_at                TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at                TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_customers_customer_number           UNIQUE (customer_number),
    CONSTRAINT uq_customers_email                     UNIQUE (email),
    CONSTRAINT uq_customers_tax_identification_number UNIQUE (tax_identification_number)
);

-- ── addresses ─────────────────────────────────────────────────────────────────
CREATE TABLE addresses
(
    id           UUID         NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY,
    customer_id  UUID         NOT NULL,
    address_type VARCHAR(20)  NOT NULL,
    street       VARCHAR(255) NOT NULL,
    city         VARCHAR(100) NOT NULL,
    state        VARCHAR(100),
    postal_code  VARCHAR(20)  NOT NULL,
    country      VARCHAR(3)   NOT NULL,
    is_primary   BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_addresses_customer FOREIGN KEY (customer_id)
        REFERENCES customers (id) ON DELETE CASCADE
);

-- ── kyc_documents ─────────────────────────────────────────────────────────────
CREATE TABLE kyc_documents
(
    id                  UUID         NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY,
    customer_id         UUID         NOT NULL,
    document_type       VARCHAR(30)  NOT NULL,
    document_number     VARCHAR(100) NOT NULL,
    issuing_authority   VARCHAR(200),
    issuing_country     VARCHAR(3)   NOT NULL,
    issue_date          DATE         NOT NULL,
    expiry_date         DATE,
    verification_status VARCHAR(30)  NOT NULL DEFAULT 'PENDING',
    verified_by         VARCHAR(100),
    verified_at         TIMESTAMP,
    rejection_reason    VARCHAR(500),
    created_at          TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_kyc_documents_customer FOREIGN KEY (customer_id)
        REFERENCES customers (id) ON DELETE CASCADE
);

-- ── risk_profiles ─────────────────────────────────────────────────────────────
CREATE TABLE risk_profiles
(
    id               UUID         NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY,
    customer_id      UUID         NOT NULL,
    risk_level       VARCHAR(20)  NOT NULL,
    risk_score       INTEGER      NOT NULL,
    assessed_at      TIMESTAMP    NOT NULL,
    assessed_by      VARCHAR(100),
    next_review_date DATE,
    assessment_notes VARCHAR(1000),
    created_at       TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_risk_profiles_customer FOREIGN KEY (customer_id)
        REFERENCES customers (id) ON DELETE CASCADE,
    CONSTRAINT uq_risk_profiles_customer_id UNIQUE (customer_id)
);

-- ── risk_factors ──────────────────────────────────────────────────────────────
CREATE TABLE risk_factors
(
    id                 UUID          NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY,
    risk_profile_id    UUID          NOT NULL,
    factor_name        VARCHAR(100)  NOT NULL,
    factor_description VARCHAR(500),
    weight             DECIMAL(5, 2) NOT NULL,
    created_at         TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_risk_factors_risk_profile FOREIGN KEY (risk_profile_id)
        REFERENCES risk_profiles (id) ON DELETE CASCADE
);

-- ── accounts ──────────────────────────────────────────────────────────────────
CREATE TABLE accounts
(
    id             UUID           NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY,
    account_number VARCHAR(30)    NOT NULL,
    customer_id    UUID           NOT NULL,
    account_type   VARCHAR(30)    NOT NULL,
    currency       VARCHAR(3)     NOT NULL DEFAULT 'USD',
    balance        DECIMAL(19, 4) NOT NULL DEFAULT 0.0000,
    account_status VARCHAR(20)    NOT NULL DEFAULT 'PENDING_ACTIVATION',
    opened_date    TIMESTAMP      NOT NULL,
    closed_date    TIMESTAMP,
    product_code   VARCHAR(20),
    interest_rate  DECIMAL(6, 4),
    created_at     TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at     TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_accounts_account_number UNIQUE (account_number),
    CONSTRAINT fk_accounts_customer FOREIGN KEY (customer_id)
        REFERENCES customers (id)
);

-- ── Indexes ───────────────────────────────────────────────────────────────────
CREATE INDEX idx_customers_email           ON customers (email);
CREATE INDEX idx_customers_customer_number ON customers (customer_number);
CREATE INDEX idx_customers_status          ON customers (customer_status);
CREATE INDEX idx_customers_onboarding_date ON customers (onboarding_date);
CREATE INDEX idx_addresses_customer_id     ON addresses (customer_id);
CREATE INDEX idx_kyc_documents_customer_id ON kyc_documents (customer_id);
CREATE INDEX idx_risk_profiles_customer_id ON risk_profiles (customer_id);
CREATE INDEX idx_accounts_customer_id      ON accounts (customer_id);
CREATE INDEX idx_accounts_status           ON accounts (account_status);

