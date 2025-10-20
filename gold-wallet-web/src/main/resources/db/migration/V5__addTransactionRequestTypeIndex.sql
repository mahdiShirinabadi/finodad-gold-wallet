-- =====================================================
-- TRANSACTION_PART TABLE INDEXES
-- =====================================================

-- Index for request_type_id field in transaction_part table
-- This index will improve performance for queries filtering by request_type_id
-- which is used in our new aggregation queries for merchant balance calculation
-- and physical cash out total quantity calculation
CREATE INDEX IF NOT EXISTS idx_transaction_part_request_type_id 
    ON transaction_part (request_type_id);

-- Composite index for wallet_account_id and request_type_id
-- This will optimize queries that filter by both fields simultaneously
CREATE INDEX IF NOT EXISTS idx_transaction_part_wallet_account_request_type 
    ON transaction_part (wallet_account_id, request_type_id);
