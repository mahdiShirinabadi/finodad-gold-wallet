-- Performance Optimization Indexes for finodad-gold-wallet
-- This migration creates essential indexes for optimal performance
-- Focused on most critical queries to minimize INSERT/UPDATE overhead

-- =====================================================
-- WALLET TABLE INDEXES (ESSENTIAL ONLY)
-- =====================================================

-- Primary lookup index for wallet by national code and wallet type (most used)
CREATE INDEX  IF NOT EXISTS idx_wallet_national_code_wallet_type_end_time
    ON wallet(national_code, wallet_type_id, end_time)
    WHERE end_time IS NULL;

-- Index for wallet created_at (for date range queries and sorting)
CREATE INDEX  IF NOT EXISTS idx_wallet_created_at
    ON wallet(DATE(created_at));

-- =====================================================
-- WALLET_ACCOUNT TABLE INDEXES (ESSENTIAL ONLY)
-- =====================================================

-- Index for wallet account lookup by wallet entity (most critical)
CREATE INDEX  IF NOT EXISTS idx_wallet_account_wallet_entity
    ON wallet_account(wallet_id);

-- Index for wallet account lookup by account number (unique constraint)
CREATE INDEX  IF NOT EXISTS idx_wallet_account_account_number
    ON wallet_account(account_number);

-- =====================================================
-- REQUEST TABLE INDEXES (ESSENTIAL ONLY)
-- =====================================================

-- Index for request lookup by id (primary key, but good for joins)
CREATE INDEX  IF NOT EXISTS idx_request_id
    ON request(id);

-- Index for request aggregation queries (most critical for reporting)
CREATE INDEX  IF NOT EXISTS idx_request_aggregation
    ON request(id, DATE(created_at), result);

-- =====================================================
-- CASH_IN_REQUEST TABLE INDEXES (ESSENTIAL ONLY)
-- =====================================================

-- Index for cash in request lookup by ref number (unique constraint)
CREATE INDEX  IF NOT EXISTS idx_cash_in_ref_number
    ON cash_in_request(ref_number);

-- Index for cash in request aggregation queries
CREATE INDEX  IF NOT EXISTS idx_cash_in_wallet_account_request
    ON cash_in_request(wallet_account_id, request_id);

-- =====================================================
-- CASH_OUT_REQUEST TABLE INDEXES (ESSENTIAL ONLY)
-- =====================================================

-- Index for cash out request aggregation queries
CREATE INDEX  IF NOT EXISTS idx_cash_out_wallet_account_request
    ON cash_out_request(wallet_account_id, request_id);

-- =====================================================
-- PURCHASE_REQUEST TABLE INDEXES (ESSENTIAL ONLY)
-- =====================================================

-- Index for purchase request aggregation queries
CREATE INDEX  IF NOT EXISTS idx_purchase_wallet_account_request_type
    ON purchase_request(wallet_account_id);

-- =====================================================
-- PHYSICAL_CASH_OUT_REQUEST TABLE INDEXES (ESSENTIAL ONLY)
-- =====================================================

-- Index for physical cash out request aggregation queries
CREATE INDEX  IF NOT EXISTS idx_physical_cash_out_wallet_account_request
    ON physical_cash_out_request(wallet_account_id);

-- =====================================================
-- RRN TABLE INDEXES (ESSENTIAL ONLY)
-- =====================================================

-- Index for rrn lookup by uuid (unique constraint)
CREATE INDEX  IF NOT EXISTS idx_rrn_uuid
    ON rrn(uuid);

-- =====================================================
-- CHANNEL_ROLE TABLE INDEXES (ESSENTIAL ONLY)
-- =====================================================

-- Index for channel role lookup by channel id and role id (most used)
CREATE INDEX  IF NOT EXISTS idx_channel_role_channel_role
    ON channel_role(channel_id, role_id);

-- =====================================================
-- ROLE_RESOURCE TABLE INDEXES (ESSENTIAL ONLY)
-- =====================================================

-- Index for role resource lookup by role id (most used)
CREATE INDEX  IF NOT EXISTS idx_role_resource_role
    ON role_resource(role_id);
