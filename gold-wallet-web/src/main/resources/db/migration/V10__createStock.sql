CREATE TABLE if not exists stock
(
    id                  BIGSERIAL PRIMARY KEY,
    created_by          VARCHAR(200)                NOT NULL,
    updated_by          VARCHAR(200),
    created_at          TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at          TIMESTAMP WITHOUT TIME ZONE,
    name                VARCHAR(100)                NOT NULL UNIQUE,
    balance           NUMERIC(25, 5)              not null,
    wallet_account_currency_id BIGINT                      NOT NULL REFERENCES wallet_account_currency,
    code CHAR(2)
);
CREATE INDEX idx_stock_code ON stock (code);
CREATE UNIQUE INDEX idx_stock_unique_code ON stock (code);

INSERT INTO stock (balance, created_by, created_at, name, wallet_account_currency_id, code)
SELECT 0,
    'system',
    now(),
    'Stock_' || LPAD(i::text, 2, '0'),  -- name like Stock_00, Stock_01 ...
    (SELECT id FROM wallet_account_currency WHERE name = 'GOLD' LIMIT 1),
    LPAD(i::text, 2, '0')  -- code as '00' to '99'
FROM generate_series(0, 99) AS s(i);

CREATE TABLE if not exists stock_history
(
    id                  BIGSERIAL PRIMARY KEY,
    created_by          VARCHAR(200)                NOT NULL,
    updated_by          VARCHAR(200),
    created_at          TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at          TIMESTAMP WITHOUT TIME ZONE,
    stock_id BIGINT                      NOT NULL REFERENCES stock,
    transaction_id BIGINT                NOT NULL,
    amount            NUMERIC(25, 5)             not null,
    type              varchar(1)                  not null,
    balance           NUMERIC(25, 5)              not null
);