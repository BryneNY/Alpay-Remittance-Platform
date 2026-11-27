-- =====================================================================
-- Alpay Remittance Platform: MySQL database schema
-- INFO 5100 - Application Engineering & Development
-- Run this once on a fresh MySQL 8 server (for example, in Docker).
-- =====================================================================

DROP DATABASE IF EXISTS alpay_remittance;
CREATE DATABASE alpay_remittance CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE alpay_remittance;

-- ---------------------------------------------------------------------
-- 1. Ecosystem structure
-- ---------------------------------------------------------------------

CREATE TABLE networks (
    network_id   INT AUTO_INCREMENT PRIMARY KEY,
    name         VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE enterprises (
    enterprise_id INT AUTO_INCREMENT PRIMARY KEY,
    network_id    INT NOT NULL,
    name          VARCHAR(100) NOT NULL UNIQUE,
    type          ENUM('MTO', 'ALPAY', 'PARTNER_BANK') NOT NULL,
    CONSTRAINT fk_enterprise_network
        FOREIGN KEY (network_id) REFERENCES networks (network_id)
);

CREATE TABLE organizations (
    organization_id INT AUTO_INCREMENT PRIMARY KEY,
    enterprise_id   INT NOT NULL,
    name            VARCHAR(100) NOT NULL,
    type            ENUM('CUSTOMER_OPERATIONS', 'COMPLIANCE_RISK',
                         'CUSTOMER_SERVICE', 'SETTLEMENT_PAYOUTS') NOT NULL,
    CONSTRAINT fk_organization_enterprise
        FOREIGN KEY (enterprise_id) REFERENCES enterprises (enterprise_id)
        ON DELETE CASCADE,
    CONSTRAINT uq_organization_per_enterprise UNIQUE (enterprise_id, type)
);

-- ---------------------------------------------------------------------
-- 2. Users and roles
-- ---------------------------------------------------------------------

CREATE TABLE persons (
    person_id  INT AUTO_INCREMENT PRIMARY KEY,
    full_name  VARCHAR(100) NOT NULL,
    email      VARCHAR(100) NOT NULL UNIQUE,
    phone      VARCHAR(20),
    country    VARCHAR(50) NOT NULL
);

-- organization_id is set for staff (MTO Officer, Compliance Officer,
-- Customer Agent, Bank Officer). enterprise_id is set for Enterprise
-- Admins and for Senders (customers of the MTO). Both are empty for
-- the System Admin.
CREATE TABLE user_accounts (
    account_id      INT AUTO_INCREMENT PRIMARY KEY,
    username        VARCHAR(50) NOT NULL UNIQUE,
    password_hash   CHAR(64) NOT NULL,
    role            ENUM('SENDER', 'MTO_OFFICER', 'COMPLIANCE_OFFICER',
                         'CUSTOMER_AGENT', 'BANK_OFFICER',
                         'ENTERPRISE_ADMIN', 'SYSTEM_ADMIN') NOT NULL,
    person_id       INT NOT NULL UNIQUE,
    organization_id INT NULL,
    enterprise_id   INT NULL,
    created_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_account_person
        FOREIGN KEY (person_id) REFERENCES persons (person_id)
        ON DELETE CASCADE,
    CONSTRAINT fk_account_organization
        FOREIGN KEY (organization_id) REFERENCES organizations (organization_id)
        ON DELETE SET NULL,
    CONSTRAINT fk_account_enterprise
        FOREIGN KEY (enterprise_id) REFERENCES enterprises (enterprise_id)
        ON DELETE SET NULL
);

-- ---------------------------------------------------------------------
-- 3. Transfers and work requests
-- ---------------------------------------------------------------------

CREATE TABLE beneficiaries (
    beneficiary_id    INT AUTO_INCREMENT PRIMARY KEY,
    sender_account_id INT NOT NULL,
    full_name         VARCHAR(100) NOT NULL,
    phone             VARCHAR(20) NOT NULL,
    payout_method     ENUM('MOMO_WALLET', 'BANK_ACCOUNT') NOT NULL,
    wallet_number     VARCHAR(20) NULL,
    bank_name         VARCHAR(100) NULL,
    account_number    VARCHAR(30) NULL,
    created_at        DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_beneficiary_sender
        FOREIGN KEY (sender_account_id) REFERENCES user_accounts (account_id)
        ON DELETE CASCADE,
    -- A MoMo beneficiary needs a wallet number; a bank beneficiary needs
    -- a bank name and account number.
    CONSTRAINT chk_payout_details CHECK (
        (payout_method = 'MOMO_WALLET'  AND wallet_number IS NOT NULL) OR
        (payout_method = 'BANK_ACCOUNT' AND bank_name IS NOT NULL
                                        AND account_number IS NOT NULL)
    )
);

CREATE TABLE transfers (
    transfer_id       INT AUTO_INCREMENT PRIMARY KEY,
    reference         VARCHAR(20) NOT NULL UNIQUE,
    sender_account_id INT NOT NULL,
    beneficiary_id    INT NOT NULL,
    amount_cad        DECIMAL(12, 2) NOT NULL,
    fee               DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    exchange_rate     DECIMAL(10, 4) NOT NULL,
    amount_ghs        DECIMAL(14, 2) NOT NULL,
    status            ENUM('SUBMITTED', 'MTO_APPROVED', 'ALPAY_APPROVED',
                           'SETTLED', 'PAID', 'RECONCILED', 'REJECTED')
                      NOT NULL DEFAULT 'SUBMITTED',
    reconciled_by     INT NULL,
    reconciled_at     DATETIME NULL,
    created_at        DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at        DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
                      ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_transfer_sender
        FOREIGN KEY (sender_account_id) REFERENCES user_accounts (account_id),
    CONSTRAINT fk_transfer_beneficiary
        FOREIGN KEY (beneficiary_id) REFERENCES beneficiaries (beneficiary_id),
    CONSTRAINT fk_transfer_reconciled_by
        FOREIGN KEY (reconciled_by) REFERENCES user_accounts (account_id)
        ON DELETE SET NULL,
    CONSTRAINT chk_amount_positive CHECK (amount_cad > 0)
);

-- All four work request types share one table. request_type says which
-- Java subclass each row belongs to; the type-specific columns are left
-- empty for the other types.
CREATE TABLE work_requests (
    request_id          INT AUTO_INCREMENT PRIMARY KEY,
    request_type        ENUM('TRANSFER_REQUEST', 'PAYMENT_INSTRUCTION',
                             'SETTLEMENT_REQUEST', 'CUSTOMER_QUERY') NOT NULL,
    transfer_id         INT NOT NULL,
    organization_id     INT NOT NULL,       -- the organization whose queue holds it
    sender_account_id   INT NOT NULL,
    receiver_account_id INT NULL,           -- empty until someone picks it up
    status              ENUM('PENDING', 'APPROVED', 'REJECTED', 'RESOLVED')
                        NOT NULL DEFAULT 'PENDING',
    message             VARCHAR(500),
    notes               VARCHAR(500) NULL,  -- verification or screening notes
    settled_amount_ghs  DECIMAL(14, 2) NULL, -- settlement requests only
    question            VARCHAR(500) NULL,  -- customer queries only
    response            VARCHAR(500) NULL,  -- customer queries only
    request_date        DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    resolve_date        DATETIME NULL,
    CONSTRAINT fk_request_transfer
        FOREIGN KEY (transfer_id) REFERENCES transfers (transfer_id)
        ON DELETE CASCADE,
    CONSTRAINT fk_request_organization
        FOREIGN KEY (organization_id) REFERENCES organizations (organization_id),
    CONSTRAINT fk_request_sender
        FOREIGN KEY (sender_account_id) REFERENCES user_accounts (account_id),
    CONSTRAINT fk_request_receiver
        FOREIGN KEY (receiver_account_id) REFERENCES user_accounts (account_id)
        ON DELETE SET NULL
);

CREATE INDEX idx_transfer_status ON transfers (status);
CREATE INDEX idx_request_queue   ON work_requests (organization_id, status);

-- =====================================================================
-- Starter data
-- Every starter account uses the password: Password123
-- Passwords are stored as SHA-256 hashes; the Java login code must hash
-- the typed password the same way before comparing.
-- =====================================================================

INSERT INTO networks (name) VALUES ('Canada'), ('Ghana');

INSERT INTO enterprises (network_id, name, type) VALUES
    (1, 'MTO Canada',         'MTO'),
    (2, 'Alpay',              'ALPAY'),
    (2, 'Partner Bank Ghana', 'PARTNER_BANK');

INSERT INTO organizations (enterprise_id, name, type) VALUES
    (1, 'Customer Operations',    'CUSTOMER_OPERATIONS'),
    (2, 'Compliance and Risk',    'COMPLIANCE_RISK'),
    (2, 'Customer Service',       'CUSTOMER_SERVICE'),
    (3, 'Settlement and Payouts', 'SETTLEMENT_PAYOUTS');

INSERT INTO persons (full_name, email, phone, country) VALUES
    ('System Administrator', 'sysadmin@alpay-demo.com',    '+233200000001', 'Ghana'),
    ('MTO Admin',            'admin@mto-demo.ca',          '+14165550101',  'Canada'),
    ('Alpay Admin',          'admin@alpay-demo.com',       '+233200000002', 'Ghana'),
    ('Bank Admin',           'admin@bank-demo.com.gh',     '+233200000003', 'Ghana'),
    ('Maria Lopez',          'officer@mto-demo.ca',        '+14165550102',  'Canada'),
    ('Kwame Mensah',         'compliance@alpay-demo.com',  '+233200000004', 'Ghana'),
    ('Ama Owusu',            'support@alpay-demo.com',     '+233200000005', 'Ghana'),
    ('Kofi Boateng',         'officer@bank-demo.com.gh',   '+233200000006', 'Ghana'),
    ('Daniel Asante',        'daniel.sender@example.com',  '+16475550103',  'Canada');

INSERT INTO user_accounts (username, password_hash, role, person_id, organization_id, enterprise_id) VALUES
    ('sysadmin',           SHA2('Password123', 256), 'SYSTEM_ADMIN',       1, NULL, NULL),
    ('mto_admin',          SHA2('Password123', 256), 'ENTERPRISE_ADMIN',   2, NULL, 1),
    ('alpay_admin',        SHA2('Password123', 256), 'ENTERPRISE_ADMIN',   3, NULL, 2),
    ('bank_admin',         SHA2('Password123', 256), 'ENTERPRISE_ADMIN',   4, NULL, 3),
    ('mto_officer',        SHA2('Password123', 256), 'MTO_OFFICER',        5, 1,    NULL),
    ('compliance_officer', SHA2('Password123', 256), 'COMPLIANCE_OFFICER', 6, 2,    NULL),
    ('customer_agent',     SHA2('Password123', 256), 'CUSTOMER_AGENT',     7, 3,    NULL),
    ('bank_officer',       SHA2('Password123', 256), 'BANK_OFFICER',       8, 4,    NULL),
    ('sender1',            SHA2('Password123', 256), 'SENDER',             9, NULL, 1);

INSERT INTO beneficiaries (sender_account_id, full_name, phone, payout_method, wallet_number, bank_name, account_number) VALUES
    (9, 'Abena Asante', '+233244000111', 'MOMO_WALLET',  '0244000111', NULL, NULL),
    (9, 'Yaw Asante',   '+233201000222', 'BANK_ACCOUNT', NULL, 'Partner Bank Ghana', '1002003004');
