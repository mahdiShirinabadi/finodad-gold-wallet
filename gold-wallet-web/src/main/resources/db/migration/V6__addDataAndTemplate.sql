insert into request_type(created_by, created_at, name, fa_name, display)
VALUES ('System', now(), 'payment_gift_card', 'مصرف کارت هدیه', 1)
    on conflict do nothing;

insert into request_type(created_by, created_at, name, fa_name, display)
VALUES ('System', now(), 'settlement', 'تسویه بانکی', 1)
    on conflict do nothing;

-- Template data insertion for all wallet operations
-- Each template includes parameters used in messageResolverOperationService.resolve()

-- Commission templates
INSERT INTO template (created_by, created_at, name, value)
VALUES ('System', now(), 'COMMISSION', 'کمیسیون ${commission}  برای ${requestType} - شناسه: ${traceId}')
    ON CONFLICT DO NOTHING;

-- P2P (Person to Person) templates
INSERT INTO template (created_by, created_at, name, value)
VALUES ('System', now(), 'p2p_deposit', 'واریز ${amount}  از ${srcNationalCode} به ${dstNationalCode} - شناسه: ${traceId}')
    ON CONFLICT DO NOTHING;

INSERT INTO template (created_by, created_at, name, value)
VALUES ('System', now(), 'p2p_withdrawal', 'برداشت ${amount}  از ${srcNationalCode} برای انتقال به ${dstNationalCode} - شناسه: ${traceId}')
    ON CONFLICT DO NOTHING;

-- Cash operations
INSERT INTO template (created_by, created_at, name, value)
VALUES ('System', now(), 'cash_in', 'واریز وجه به مبلغ ${amount}  - شناسه: ${traceId}')
    ON CONFLICT DO NOTHING;

INSERT INTO template (created_by, created_at, name, value)
VALUES ('System', now(), 'cash_out', 'برداشت وجه به مبلغ ${amount}  - شناسه: ${traceId}')
    ON CONFLICT DO NOTHING;

INSERT INTO template (created_by, created_at, name, value)
VALUES ('System', now(), 'physical_cash_out', 'برداشت فیزیکی وجه به مبلغ ${amount}  - شناسه: ${traceId}')
    ON CONFLICT DO NOTHING;

-- Buy/Sell operations
INSERT INTO template (created_by, created_at, name, value)
VALUES ('System', now(), 'BUY_WITHDRAWAL', 'برداشت ${amount}  برای خرید از ${merchant} - شناسه: ${traceId}')
    ON CONFLICT DO NOTHING;

INSERT INTO template (created_by, created_at, name, value)
VALUES ('System', now(), 'BUY_DEPOSIT', 'واریز ${amount}  از ${merchant} - شناسه: ${traceId}')
    ON CONFLICT DO NOTHING;

INSERT INTO template (created_by, created_at, name, value)
VALUES ('System', now(), 'SELL_WITHDRAWAL', 'برداشت ${amount}  برای فروش به ${merchant} - شناسه: ${traceId}')
    ON CONFLICT DO NOTHING;

INSERT INTO template (created_by, created_at, name, value)
VALUES ('System', now(), 'SELL_DEPOSIT', 'واریز ${price}  از فروش به ${merchant} - شناسه: ${traceId}')
    ON CONFLICT DO NOTHING;

-- Gift Card operations
INSERT INTO template (created_by, created_at, name, value)
VALUES ('System', now(), 'GIFT_CARD_WITHDRAWAL', 'برداشت ${amount}  برای کارت هدیه از ${srcNationalCode} به ${dstNationalCode} - شناسه: ${traceId}')
    ON CONFLICT DO NOTHING;

INSERT INTO template (created_by, created_at, name, value)
VALUES ('System', now(), 'GIFT_CARD_DEPOSIT', 'واریز ${amount}  از کارت هدیه از ${srcNationalCode} به ${dstNationalCode} - شناسه: ${traceId}')
    ON CONFLICT DO NOTHING;

-- Collateral Create operations
INSERT INTO template (created_by, created_at, name, value)
VALUES ('System', now(), 'COLLATERAL_CREATE_WITHDRAWAL', 'برداشت ${amount}  برای ایجاد وثیقه ${collateralName} - شماره حساب: ${accountNumber} - شناسه: ${traceId}')
    ON CONFLICT DO NOTHING;

INSERT INTO template (created_by, created_at, name, value)
VALUES ('System', now(), 'COLLATERAL_CREATE_DEPOSIT', 'واریز ${amount}  برای ایجاد وثیقه ${collateralName} - شماره حساب: ${accountNumber} - شناسه: ${traceId}')
    ON CONFLICT DO NOTHING;

-- Collateral Release operations
INSERT INTO template (created_by, created_at, name, value)
VALUES ('System', now(), 'COLLATERAL_RELEASE_WITHDRAWAL', 'برداشت ${amount}  برای آزادسازی وثیقه - شماره حساب: ${accountNumber} - شناسه: ${traceId}')
    ON CONFLICT DO NOTHING;

INSERT INTO template (created_by, created_at, name, value)
VALUES ('System', now(), 'COLLATERAL_RELEASE_DEPOSIT', 'واریز ${amount}  برای آزادسازی وثیقه - شماره حساب: ${accountNumber} - شناسه: ${traceId}')
    ON CONFLICT DO NOTHING;

-- Collateral Increase operations
INSERT INTO template (created_by, created_at, name, value)
VALUES ('System', now(), 'COLLATERAL_INCREASE_WITHDRAWAL', 'برداشت ${amount}  برای افزایش وثیقه - شماره حساب: ${accountNumber} - شناسه: ${traceId}')
    ON CONFLICT DO NOTHING;

INSERT INTO template (created_by, created_at, name, value)
VALUES ('System', now(), 'COLLATERAL_INCREASE_DEPOSIT', 'واریز ${amount}  برای افزایش وثیقه - شماره حساب: ${accountNumber} - شناسه: ${traceId}')
    ON CONFLICT DO NOTHING;

-- Collateral Seize operations
INSERT INTO template (created_by, created_at, name, value)
VALUES ('System', now(), 'COLLATERAL_SEIZE_WITHDRAWAL', 'برداشت ${amount}  برای توقیف وثیقه از ${srcAccountNumber} به ${dstCompany} - شناسه: ${traceId}')
    ON CONFLICT DO NOTHING;

INSERT INTO template (created_by, created_at, name, value)
VALUES ('System', now(), 'COLLATERAL_SEIZE_DEPOSIT', 'واریز ${amount}  برای توقیف وثیقه از ${srcAccountNumber} به ${dstCompany} - شناسه: ${traceId}')
    ON CONFLICT DO NOTHING;

-- Collateral Sell operations
INSERT INTO template (created_by, created_at, name, value)
VALUES ('System', now(), 'COLLATERAL_SELL_WITHDRAWAL', 'برداشت ${amount} ریال برای فروش وثیقه - شناسه: ${traceId}')
    ON CONFLICT DO NOTHING;

INSERT INTO template (created_by, created_at, name, value)
VALUES ('System', now(), 'COLLATERAL_SELL_DEPOSIT', 'واریز ${amount} ریال برای فروش وثیقه - شناسه: ${traceId}')
    ON CONFLICT DO NOTHING;

-- Collateral Return After Sell operations
INSERT INTO template (created_by, created_at, name, value)
VALUES ('System', now(), 'COLLATERAL_RETURN_AFTER_SELL_DEPOSIT', 'واریز ${amount} ریال برای بازگشت وثیقه پس از فروش - شناسه: ${traceId}')
    ON CONFLICT DO NOTHING;

CREATE TABLE if not exists fund_transfer_account_to_account_request
(
    request_id            BIGSERIAL,
    from_account          VARCHAR(100),
    to_account            VARCHAR(100),
    rrn_id                BIGINT NOT NULL REFERENCES rrn,
    ref_number            VARCHAR(100),
    national_code         VARCHAR(10),
    channel_request_time  TIMESTAMP WITHOUT TIME ZONE,
    channel_response_time TIMESTAMP WITHOUT TIME ZONE,
    channel_response      TEXT,
    amount                BIGINT,
    channel_result        VARCHAR(100),
    trace_number          VARCHAR(100),
    step_status           VARCHAR(100),
    additional_data       VARCHAR(200),
    multi_transaction     BOOLEAN DEFAULT false NOT NULL
);



CREATE TABLE if not exists cash_out_fund_transfer
(
    id                  BIGSERIAL PRIMARY KEY,
    created_by          VARCHAR(200)                NOT NULL,
    updated_by          VARCHAR(200),
    created_at          TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at          TIMESTAMP WITHOUT TIME ZONE,
    cash_out_request_id  BIGINT   NOT NULL,
    fund_transfer_account_to_account_request_id BIGINT   NOT NULL
);

CREATE TABLE if not exists settlement
(
    id                  BIGSERIAL PRIMARY KEY,
    created_by          VARCHAR(200)                NOT NULL,
    updated_by          VARCHAR(200),
    created_at          TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at          TIMESTAMP WITHOUT TIME ZONE,
    cash_out_request_id  BIGINT   NOT NULL,
    fund_transfer_account_to_account_request_id BIGINT   NOT NULL
);

