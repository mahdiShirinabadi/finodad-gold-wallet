insert into request_type(created_by, created_at, name, fa_name, display)
VALUES ('System', now(), 'payment_gift_card', 'مصرف کارت هدیه', 1)
    on conflict do nothing;

-- Template data insertion for all wallet operations
-- Each template includes parameters used in messageResolverOperationService.resolve()

-- Commission templates
INSERT INTO template (created_by, created_at, name, value)
VALUES ('System', now(), 'COMMISSION', 'کمیسیون {commission} ریال برای {requestType} - شناسه: {traceId}')
    ON CONFLICT DO NOTHING;

-- P2P (Person to Person) templates
INSERT INTO template (created_by, created_at, name, value)
VALUES ('System', now(), 'p2p_deposit', 'واریز {amount} ریال از {srcNationalCode} به {dstNationalCode} - شناسه: {traceId}')
    ON CONFLICT DO NOTHING;

INSERT INTO template (created_by, created_at, name, value)
VALUES ('System', now(), 'p2p_withdrawal', 'برداشت {amount} ریال از {srcNationalCode} برای انتقال به {dstNationalCode} - شناسه: {traceId}')
    ON CONFLICT DO NOTHING;

-- Cash operations
INSERT INTO template (created_by, created_at, name, value)
VALUES ('System', now(), 'cash_in', 'واریز وجه به مبلغ {amount} ریال - شناسه: {traceId}')
    ON CONFLICT DO NOTHING;

INSERT INTO template (created_by, created_at, name, value)
VALUES ('System', now(), 'cash_out', 'برداشت وجه به مبلغ {amount} ریال - شناسه: {traceId}')
    ON CONFLICT DO NOTHING;

INSERT INTO template (created_by, created_at, name, value)
VALUES ('System', now(), 'physical_cash_out', 'برداشت فیزیکی وجه به مبلغ {amount} ریال - شناسه: {traceId}')
    ON CONFLICT DO NOTHING;

-- Buy/Sell operations
INSERT INTO template (created_by, created_at, name, value)
VALUES ('System', now(), 'BUY_WITHDRAWAL', 'برداشت {amount} ریال برای خرید طلا از {merchant} - شناسه: {traceId}')
    ON CONFLICT DO NOTHING;

INSERT INTO template (created_by, created_at, name, value)
VALUES ('System', now(), 'BUY_DEPOSIT', 'واریز {amount} گرم طلا از {merchant} - شناسه: {traceId}')
    ON CONFLICT DO NOTHING;

INSERT INTO template (created_by, created_at, name, value)
VALUES ('System', now(), 'SELL_WITHDRAWAL', 'برداشت {amount} گرم طلا برای فروش به {merchant} - شناسه: {traceId}')
    ON CONFLICT DO NOTHING;

INSERT INTO template (created_by, created_at, name, value)
VALUES ('System', now(), 'SELL_DEPOSIT', 'واریز {price} ریال از فروش طلا به {merchant} - شناسه: {traceId}')
    ON CONFLICT DO NOTHING;

-- Gift Card operations
INSERT INTO template (created_by, created_at, name, value)
VALUES ('System', now(), 'GIFT_CARD_WITHDRAWAL', 'برداشت {amount} ریال برای کارت هدیه از {srcNationalCode} به {dstNationalCode} - شناسه: {traceId}')
    ON CONFLICT DO NOTHING;

INSERT INTO template (created_by, created_at, name, value)
VALUES ('System', now(), 'GIFT_CARD_DEPOSIT', 'واریز {amount} ریال از کارت هدیه از {srcNationalCode} به {dstNationalCode} - شناسه: {traceId}')
    ON CONFLICT DO NOTHING;

-- Collateral Create operations
INSERT INTO template (created_by, created_at, name, value)
VALUES ('System', now(), 'COLLATERAL_CREATE_WITHDRAWAL', 'برداشت {amount} ریال برای ایجاد وثیقه {collateralName} - شماره حساب: {accountNumber} - شناسه: {traceId}')
    ON CONFLICT DO NOTHING;

INSERT INTO template (created_by, created_at, name, value)
VALUES ('System', now(), 'COLLATERAL_CREATE_DEPOSIT', 'واریز {amount} ریال برای ایجاد وثیقه {collateralName} - شماره حساب: {accountNumber} - شناسه: {traceId}')
    ON CONFLICT DO NOTHING;

-- Collateral Release operations
INSERT INTO template (created_by, created_at, name, value)
VALUES ('System', now(), 'COLLATERAL_RELEASE_WITHDRAWAL', 'برداشت {amount} ریال برای آزادسازی وثیقه - شماره حساب: {accountNumber} - شناسه: {traceId}')
    ON CONFLICT DO NOTHING;

INSERT INTO template (created_by, created_at, name, value)
VALUES ('System', now(), 'COLLATERAL_RELEASE_DEPOSIT', 'واریز {amount} ریال برای آزادسازی وثیقه - شماره حساب: {accountNumber} - شناسه: {traceId}')
    ON CONFLICT DO NOTHING;

-- Collateral Increase operations
INSERT INTO template (created_by, created_at, name, value)
VALUES ('System', now(), 'COLLATERAL_INCREASE_WITHDRAWAL', 'برداشت {amount} ریال برای افزایش وثیقه - شماره حساب: {accountNumber} - شناسه: {traceId}')
    ON CONFLICT DO NOTHING;

INSERT INTO template (created_by, created_at, name, value)
VALUES ('System', now(), 'COLLATERAL_INCREASE_DEPOSIT', 'واریز {amount} ریال برای افزایش وثیقه - شماره حساب: {accountNumber} - شناسه: {traceId}')
    ON CONFLICT DO NOTHING;

-- Collateral Seize operations
INSERT INTO template (created_by, created_at, name, value)
VALUES ('System', now(), 'COLLATERAL_SEIZE_WITHDRAWAL', 'برداشت {amount} ریال برای توقیف وثیقه از {srcAccountNumber} به {dstCompany} - شناسه: {traceId}')
    ON CONFLICT DO NOTHING;

INSERT INTO template (created_by, created_at, name, value)
VALUES ('System', now(), 'COLLATERAL_SEIZE_DEPOSIT', 'واریز {amount} ریال برای توقیف وثیقه از {srcAccountNumber} به {dstCompany} - شناسه: {traceId}')
    ON CONFLICT DO NOTHING;

-- Collateral Sell operations
INSERT INTO template (created_by, created_at, name, value)
VALUES ('System', now(), 'COLLATERAL_SELL_WITHDRAWAL', 'برداشت {amount} ریال برای فروش وثیقه - شناسه: {traceId}')
    ON CONFLICT DO NOTHING;

INSERT INTO template (created_by, created_at, name, value)
VALUES ('System', now(), 'COLLATERAL_SELL_DEPOSIT', 'واریز {amount} ریال برای فروش وثیقه - شناسه: {traceId}')
    ON CONFLICT DO NOTHING;

-- Collateral Return After Sell operations
INSERT INTO template (created_by, created_at, name, value)
VALUES ('System', now(), 'COLLATERAL_RETURN_AFTER_SELL_DEPOSIT', 'واریز {amount} ریال برای بازگشت وثیقه پس از فروش - شناسه: {traceId}')
    ON CONFLICT DO NOTHING;
