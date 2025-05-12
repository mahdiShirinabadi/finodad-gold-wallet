create table if not exists channel
(
    id                 serial                      not null primary key,
    created_by         VARCHAR(200)                NOT NULL,
    updated_by         VARCHAR(200),
    created_at         TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at         TIMESTAMP WITHOUT TIME ZONE,
    firstname          varchar(500)  default null,
    lastname           varchar(500)  default null,
    username           varchar(50) unique          not null,
    mobile             varchar(50),
    password           varchar(500)  default null,
    trust              int           default 0,
    sign               int           default 0,
    public_key         varchar(1000) default null,
    ip                 varchar(500)  default null,
    status             varchar(500)  default null,
    iban               varchar(13)   default null,
    wallet_id          bigint        default null,
    account            varchar(13)   default null,
    national_code      char(10)      default null,
    check_shahkar      int           default 0,
    end_time           TIMESTAMP WITHOUT TIME ZONE
);

CREATE TABLE if not exists role_
(
    id                  BIGSERIAL PRIMARY KEY,
    created_by          VARCHAR(200)                NOT NULL,
    updated_by          VARCHAR(200),
    created_at          TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at          TIMESTAMP WITHOUT TIME ZONE,
    name                VARCHAR(100)                NOT NULL UNIQUE,
    persian_description VARCHAR(100),
    additional_data     VARCHAR(100),
    end_time            TIMESTAMP WITHOUT TIME ZONE
);
CREATE INDEX idx_role_name ON role_(name);
CREATE UNIQUE INDEX idx_role_unique_name ON role_(name);

CREATE TABLE if not exists resource
(
    id         BIGSERIAL PRIMARY KEY,
    created_by VARCHAR(200)                NOT NULL,
    updated_by VARCHAR(200),
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITHOUT TIME ZONE,
    name       VARCHAR(100)                NOT NULL UNIQUE,
    fa_name    VARCHAR(100)                NOT NULL,
    display    INTEGER
);
CREATE INDEX idx_resource_name ON resource(name);
CREATE UNIQUE INDEX idx_resource_unique_name ON resource(name);

CREATE TABLE if not exists request_type
(
    id         BIGSERIAL PRIMARY KEY,
    created_by VARCHAR(200)                NOT NULL,
    updated_by VARCHAR(200),
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITHOUT TIME ZONE,
    name       VARCHAR(100)                NOT NULL UNIQUE,
    fa_name    VARCHAR(100)                NOT NULL,
    display    INTEGER
);
CREATE INDEX idx_request_type_name ON request_type(name);
CREATE UNIQUE INDEX idx_request_type_unique_name ON request_type(name);

CREATE TABLE if not exists channel_access_token
(
    id                        BIGSERIAL PRIMARY KEY,
    created_by                VARCHAR(200)                NOT NULL,
    updated_by                VARCHAR(200),
    created_at                TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at                TIMESTAMP WITHOUT TIME ZONE,
    channel_id                BIGINT                      NOT NULL REFERENCES channel,
    access_token              VARCHAR(400)                NOT NULL,
    refresh_token             VARCHAR(400)                NOT NULL,
    ip                        VARCHAR(500) DEFAULT NULL::CHARACTER VARYING,
    access_token_expire_time  TIMESTAMP WITHOUT TIME ZONE,
    refresh_token_expire_time TIMESTAMP WITHOUT TIME ZONE,
    end_time                  TIMESTAMP WITHOUT TIME ZONE
);


CREATE TABLE if not exists channel_block
(
    id               BIGSERIAL PRIMARY KEY,
    created_by       VARCHAR(200)                NOT NULL,
    updated_by       VARCHAR(200),
    created_at       TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at       TIMESTAMP WITHOUT TIME ZONE,
    channel_id       BIGINT                      NOT NULL REFERENCES channel,
    start_block_date TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    end_block_date   TIMESTAMP WITHOUT TIME ZONE,
    count_fail       INTEGER                     NOT NULL
);

CREATE TABLE if not exists role_resource
(
    id          BIGSERIAL PRIMARY KEY,
    created_by  VARCHAR(200)                NOT NULL,
    updated_by  VARCHAR(200),
    created_at  TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at  TIMESTAMP WITHOUT TIME ZONE NULL,
    role_id     BIGINT                      NOT NULL REFERENCES role_,
    resource_id BIGINT                      NOT NULL REFERENCES resource
);

CREATE TABLE if not exists channel_role
(
    id         BIGSERIAL PRIMARY KEY,
    created_by VARCHAR(200)                NOT NULL,
    updated_by VARCHAR(200),
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITHOUT TIME ZONE NULL,
    role_id    BIGINT                      NOT NULL REFERENCES role_,
    channel_id BIGINT                      NOT NULL REFERENCES channel
);

CREATE TABLE if not exists wallet_account_currency
(
    id              BIGSERIAL PRIMARY KEY,
    created_by      VARCHAR(200)                NOT NULL,
    updated_by      VARCHAR(200),
    created_at      TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at      TIMESTAMP WITHOUT TIME ZONE,
    name            VARCHAR(100),
    suffix          VARCHAR(100),
    additional_data VARCHAR(300),
    description     VARCHAR(100)
);
CREATE INDEX idx_wallet_account_currency_name ON wallet_account_currency(name);
CREATE UNIQUE INDEX idx_wallet_account_currency_unique_name ON wallet_account_currency(LOWER(name));
insert into wallet_account_currency(created_by, created_at, name, suffix, additional_data, description)
VALUES ('System',now(), 'GOLD','گرم','','طلا');
insert into wallet_account_currency(created_by, created_at, name, suffix, additional_data, description)
VALUES ('System',now(), 'RIAL','ریال','','ریال');

CREATE TABLE if not exists wallet_type
(
    id         BIGSERIAL PRIMARY KEY,
    created_by VARCHAR(200)                NOT NULL,
    updated_by VARCHAR(200),
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITHOUT TIME ZONE,
    name       VARCHAR(100)
);
CREATE INDEX idx_wallet_type_name ON wallet_type(name);
CREATE UNIQUE INDEX idx_wallet_type_unique_name ON wallet_type(LOWER(name));
insert into wallet_type(created_by, created_at, name) values ('System',now(),'NORMAL_USER');
insert into wallet_type(created_by, created_at, name) values ('System',now(),'CHANNEL');
insert into wallet_type(created_by, created_at, name) values ('System',now(),'MERCHANT');


CREATE TABLE if not exists wallet_account_type
(
    id              BIGSERIAL PRIMARY KEY,
    created_by      VARCHAR(200)                NOT NULL,
    updated_by      VARCHAR(200),
    created_at      TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at      TIMESTAMP WITHOUT TIME ZONE,
    name            VARCHAR(100),
    additional_data VARCHAR(300),
    description     VARCHAR(100)
);
insert into wallet_account_type(created_by, created_at, name, additional_data, description)
VALUES ('System',now(), 'NORMAL','عادی','NORMAL');
insert into wallet_account_type(created_by, created_at, name, additional_data, description)
VALUES ('System',now(), 'WAGE','کارمزد','WAGE');

CREATE TABLE if not exists wallet_level
(
    id         BIGSERIAL PRIMARY KEY,
    created_by VARCHAR(200)                NOT NULL,
    updated_by VARCHAR(200),
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITHOUT TIME ZONE,
    name       VARCHAR(100)
);

insert into wallet_level(created_by, created_at, name) values ('System',now(),'BRONZE');
insert into wallet_level(created_by, created_at, name) values ('System',now(),'SILVER');
insert into wallet_level(created_by, created_at, name) values ('System',now(),'GOLD');
insert into wallet_level(created_by, created_at, name) values ('System',now(),'PLATINUM');

CREATE TABLE if not exists wallet
(
    id               BIGSERIAL PRIMARY KEY,
    created_by       VARCHAR(200)                NOT NULL,
    updated_by       VARCHAR(200),
    created_at       TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at       TIMESTAMP WITHOUT TIME ZONE,
    mobile           VARCHAR(100),
    national_code    VARCHAR(100),
    description      VARCHAR(100),
    identifier       VARCHAR(100),
    level_           INTEGER,
    status           VARCHAR(100),
    owner_channel_id BIGINT                      NOT NULL REFERENCES channel,
    wallet_type_id   BIGINT                      NOT NULL REFERENCES wallet_type,
    wallet_level_id  BIGINT                      NOT NULL REFERENCES wallet_level,
    end_time         TIMESTAMP WITHOUT TIME ZONE
);

CREATE TABLE if not exists wallet_account
(
    id                         BIGSERIAL PRIMARY KEY,
    created_by                 VARCHAR(200)                NOT NULL,
    updated_by                 VARCHAR(200),
    created_at                 TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at                 TIMESTAMP WITHOUT TIME ZONE,
    wallet_id                  BIGINT                      NOT NULL REFERENCES wallet,
    wallet_account_type_id     BIGINT                      NOT NULL REFERENCES wallet_account_type,
    wallet_account_currency_id BIGINT                      NOT NULL REFERENCES wallet_account_currency,
    account_number             VARCHAR(100),
    status                     VARCHAR(100),
    version                    BIGINT,
    pin                        VARCHAR(100),
    partner_id                 INTEGER,
    balance                    BIGINT default 0,
    end_time                   TIMESTAMP WITHOUT TIME ZONE
);

CREATE TABLE if not exists wallet_iban
(
    id                BIGSERIAL PRIMARY KEY,
    created_by        VARCHAR(200)                NOT NULL,
    updated_by        VARCHAR(200),
    created_at        TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at        TIMESTAMP WITHOUT TIME ZONE,
    wallet_account_id BIGINT                      NOT NULL REFERENCES wallet_account,
    national_code     VARCHAR(100),
    birth_date        VARCHAR(100),
    iban              VARCHAR(100),
    account_number    VARCHAR(100),
    account_bank_name VARCHAR(100),
    account_owner     VARCHAR(100)
);

CREATE TABLE if not exists setting_general
(
    id              BIGSERIAL PRIMARY KEY,
    created_by      VARCHAR(200)                NOT NULL,
    updated_by      VARCHAR(200),
    created_at      TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at      TIMESTAMP WITHOUT TIME ZONE,
    name            VARCHAR(100)                NOT NULL UNIQUE,
    value           VARCHAR(100),
    pattern         VARCHAR(100),
    additional_data VARCHAR(100),
    end_time        TIMESTAMP WITHOUT TIME ZONE
);

CREATE TABLE if not exists limitation_general
(
    id              BIGSERIAL PRIMARY KEY,
    created_by      VARCHAR(200)                NOT NULL,
    updated_by      VARCHAR(200),
    created_at      TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at      TIMESTAMP WITHOUT TIME ZONE,
    name            VARCHAR(100)                NOT NULL UNIQUE,
    value           VARCHAR(100),
    pattern         VARCHAR(100),
    additional_data VARCHAR(100),
    end_time        TIMESTAMP WITHOUT TIME ZONE
);

CREATE TABLE if not exists status
(
    id                  BIGSERIAL PRIMARY KEY,
    created_by          VARCHAR(200)                NOT NULL,
    updated_by          VARCHAR(200),
    created_at          TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at          TIMESTAMP WITHOUT TIME ZONE,
    code                VARCHAR(100)                NOT NULL UNIQUE,
    persian_description VARCHAR(500),
    additional_data     VARCHAR(500)
);

CREATE TABLE if not exists version
(
    id              BIGSERIAL PRIMARY KEY,
    created_by      VARCHAR(200)                NOT NULL,
    updated_by      VARCHAR(200),
    created_at      TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at      TIMESTAMP WITHOUT TIME ZONE,
    version_number  VARCHAR(200)                NOT NULL,
    changes         VARCHAR(200)                NOT NULL,
    additional_data VARCHAR(200),
    active          BOOLEAN                     NOT NULL,
    end_time        TIMESTAMP WITHOUT TIME ZONE
);

CREATE TABLE if not exists shahkar_info
(
    id                    BIGSERIAL PRIMARY KEY,
    created_by            VARCHAR(200)                NOT NULL,
    updated_by            VARCHAR(200),
    created_at            TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at            TIMESTAMP WITHOUT TIME ZONE,
    national_code         VARCHAR(100),
    mobile                VARCHAR(100),
    channel_request_time  TIMESTAMP WITHOUT TIME ZONE,
    channel_response_time TIMESTAMP WITHOUT TIME ZONE,
    channel_response      TEXT,
    is_match              boolean
);

CREATE TABLE if not exists rrn
(
    id            BIGSERIAL PRIMARY KEY,
    created_by    VARCHAR(200)                NOT NULL,
    updated_by    VARCHAR(200),
    created_at    TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at    TIMESTAMP WITHOUT TIME ZONE,
    national_code VARCHAR(10)                 NOT NULL,
    channel_id    BIGINT                      NOT NULL REFERENCES channel,
    request_type_id BIGINT                      NOT NULL REFERENCES request_type,
    extra_data    text,
    uuid          VARCHAR(100) default gen_random_uuid() || '-' || EXTRACT(EPOCH FROM NOW())
);
CREATE UNIQUE INDEX idx_unique_uuid_rrn ON rrn (uuid);

CREATE TABLE if not exists request
(
    id              BIGSERIAL PRIMARY KEY,
    created_by      VARCHAR(200)                NOT NULL,
    updated_by      VARCHAR(200),
    created_at      TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at      TIMESTAMP WITHOUT TIME ZONE,
    channel_id      BIGINT                      NOT NULL REFERENCES channel,
    request_type_id BIGINT                      NOT NULL REFERENCES request_type,
    result          INTEGER                     NOT NULL,
    channel_ip      VARCHAR(200),
    customer_ip     VARCHAR(200)
);

CREATE TABLE if not exists merchant
(
    id              BIGSERIAL PRIMARY KEY,
    created_by      VARCHAR(200)                NOT NULL,
    updated_by      VARCHAR(200),
    created_at      TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at      TIMESTAMP WITHOUT TIME ZONE,
    name            VARCHAR(200)                NOT NULL UNIQUE,
    mobile          VARCHAR(200)                NOT NULL,
    national_code   VARCHAR(200)                NOT NULL,
    economical_code VARCHAR(200)                NOT NULL,
    pay_id          VARCHAR(200)                NOT NULL,
    wallet_id       BIGINT                      NOT NULL REFERENCES wallet,
    settlement_type INTEGER,
    status          INTEGER,
    end_time        TIMESTAMP WITHOUT TIME ZONE
);

CREATE TABLE if not exists deactivate_wallet_account_request
(
    id                BIGSERIAL PRIMARY KEY,
    created_by        VARCHAR(200)                NOT NULL,
    updated_by        VARCHAR(200),
    created_at        TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at        TIMESTAMP WITHOUT TIME ZONE,
    wallet_account_id BIGINT                      NOT NULL REFERENCES wallet_account
);

CREATE TABLE if not exists deactivate_wallet_request
(
    id         BIGSERIAL PRIMARY KEY,
    created_by VARCHAR(200)                NOT NULL,
    updated_by VARCHAR(200),
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITHOUT TIME ZONE,
    wallet_id  BIGINT                      NOT NULL REFERENCES wallet
);

CREATE TABLE if not exists create_wallet_request
(
    request_id    BIGINT NOT NULL REFERENCES request,
    national_code VARCHAR(100)
);

CREATE TABLE if not exists purchase_request
(
    request_id                  BIGINT primary key NOT NULL REFERENCES request,
    transaction_type            VARCHAR(50)        NOT NULL CHECK (transaction_type IN ('BUY', 'SELL')),
    price                       BIGINT,         -- Purchased quantity price
    quantity                    NUMERIC(10, 5), -- Purchased quantity
    wallet_account_id           BIGINT             NOT NULL REFERENCES wallet_account,
    rrn_id                      BIGINT             NOT NULL REFERENCES rrn,
    merchant_id                 BIGINT             NOT NULL REFERENCES merchant,
    national_code               VARCHAR(100),
    terminal_amount             VARCHAR(100),
    additional_data             VARCHAR(500),
    ref_number                  VARCHAR(500),
    commission           NUMERIC(10, 5)
);
create unique index rrn_id_purchase_request_unique_idx on purchase_request (rrn_id);

CREATE TABLE if not exists p_2_p_request
(
    request_id             BIGINT NOT NULL REFERENCES request,
    rrn_id                 BIGINT NOT NULL REFERENCES rrn,
    amount                 NUMERIC(10, 5),
    src_wallet_account_id  BIGINT NOT NULL REFERENCES wallet_account,
    dest_wallet_account_id BIGINT NOT NULL REFERENCES wallet_account
);
create unique index rrn_id_p_2_p_request_unique_idx on p_2_p_request (rrn_id);

CREATE TABLE if not exists cash_in_request
(
    request_id           BIGINT NOT NULL primary key REFERENCES request,
    amount               BIGINT, -- Purchased quantity
    wallet_account_id    BIGINT NOT NULL REFERENCES wallet_account,
    rrn_id               BIGINT NOT NULL REFERENCES rrn,
    psp_token            VARCHAR(200),
    psp_response         VARCHAR(500),
    additional_data      VARCHAR(500),
    ref_number           VARCHAR(500),
    cash_in_payment_type VARCHAR(100),
    psp_request_time     TIMESTAMP WITHOUT TIME ZONE,
    psp_response_time    TIMESTAMP WITHOUT TIME ZONE
);
create unique index payment_id_cash_in_request_unique_idx on cash_in_request (ref_number);
create unique index rrn_id_cash_in_request_unique_idx on cash_in_request (rrn_id);

CREATE TABLE if not exists cash_in_history_request
(
    id                  BIGSERIAL PRIMARY KEY,
    created_by          VARCHAR(200)                NOT NULL,
    updated_by          VARCHAR(200),
    created_at          TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at          TIMESTAMP WITHOUT TIME ZONE,
    cash_in_ipg_request BIGINT                      NOT NULL REFERENCES cash_in_request,
    psp_response        VARCHAR(500),
    additional_data     VARCHAR(500),
    ref_number          VARCHAR(200),
    psp_request_time    TIMESTAMP WITHOUT TIME ZONE,
    psp_response_time   TIMESTAMP WITHOUT TIME ZONE,
    psp_step            VARCHAR(50)                 NOT NULL CHECK (psp_step IN ('CREATE', 'GET_TOKEN', 'REDIRECT_IPG',
                                                                                 'CALL_BACK_FROM_PSP', 'VERIFY',
                                                                                 'REVERSE'))
);

CREATE TABLE if not exists cash_in_special_request
(
    request_id        BIGINT NOT NULL REFERENCES request,
    amount            BIGINT, -- Purchased quantity
    ref_number        VARCHAR(500),
    wallet_account_id BIGINT NOT NULL REFERENCES wallet_account,
    rrn_id            BIGINT NOT NULL REFERENCES rrn,
    additional_data   VARCHAR(500)
);
create unique index payment_id_cash_in_special_request_unique_idx on cash_in_special_request (ref_number);
create unique index rrn_id_cash_in_special_request_unique_idx on cash_in_special_request (rrn_id);

CREATE TABLE if not exists cash_out_request
(
    request_id        BIGINT NOT NULL REFERENCES request,
    amount            BIGINT, -- Purchased quantity
    iban              VARCHAR(100),
    wallet_account_id BIGINT NOT NULL REFERENCES wallet_account,
    rrn_id            BIGINT NOT NULL REFERENCES rrn,
    additional_data   VARCHAR(500)
);
create unique index rrn_id_cash_out_request_unique_idx on cash_out_request (rrn_id);

CREATE TABLE if not exists limitation_general_custom
(
    id                         BIGSERIAL PRIMARY KEY,
    created_by                 VARCHAR(200)                NOT NULL,
    updated_by                 VARCHAR(200),
    created_at                 TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at                 TIMESTAMP WITHOUT TIME ZONE,
    limitation_general_id         BIGINT                      not null references limitation_general,
    wallet_level_id            BIGINT                      not null references wallet_level,
    channel_id                 BIGINT                      not null references channel,
    wallet_account_type_id     BIGINT                      not null references wallet_account_type,
    wallet_account_currency_id BIGINT                      not null references wallet_account_currency,
    wallet_type_id             BIGINT                      not null references wallet_type,
    value                      VARCHAR(100),
    additional_data            VARCHAR(200),
    end_time                   TIMESTAMP WITHOUT TIME ZONE
);

CREATE TABLE if not exists template
(
    id         BIGSERIAL PRIMARY KEY,
    created_by VARCHAR(200)                NOT NULL,
    updated_by VARCHAR(200),
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITHOUT TIME ZONE,
    name  varchar(100),
    value text
);

CREATE TABLE if not exists transaction_part
(
    id         BIGSERIAL,
    created_by VARCHAR(200)                NOT NULL,
    updated_by VARCHAR(200),
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITHOUT TIME ZONE,
    wallet_account_id int                         not null references wallet_account (id),
    request_type_id   int,
    amount            bigint                      not null,
    type              varchar(1)                  not null,
    balance           bigint                      not null,
    description       varchar(200) default null,
    additional_data   varchar(300) default null,
    rrn_id            bigint,
    primary key (id, wallet_account_id)
) PARTITION BY RANGE (wallet_account_id);


insert into role_ (created_by, created_at, name, persian_description, additional_data)
values ('admin', now(), 'WEB_PROFILE', 'کاربر وب', '')
on conflict do nothing;

INSERT INTO setting_general(created_by, created_at, name, value, additional_data)
VALUES ('System', now(), 'MAX_WRONG_PASSWORD_FOR_PROFILE', '5', 'حداکثر تعداد رمز نادرست')
on conflict do nothing;

INSERT INTO setting_general(created_by, created_at, name, value, additional_data)
VALUES ('System', now(), 'DURATION_ACCESS_TOKEN_PROFILE', '600', 'زمان توکن پروفایل')
on conflict do nothing;

INSERT INTO setting_general(created_by, created_at, name, value, additional_data)
VALUES ('System', now(), 'DURATION_REFRESH_TOKEN_PROFILE', '86400', 'زمان توکن پروفایل')
on conflict do nothing;

INSERT INTO setting_general(created_by, created_at, name, value, additional_data)
VALUES ('System', now(), 'MAX_OTP_EXPIRE_TIME_MINUTES', '3', 'حداکثر زمان otp')
on conflict do nothing;

INSERT INTO setting_general(created_by, created_at, name, value, additional_data)
VALUES ('System', now(), 'LENGTH_OTP', '5', 'طول رمز')
on conflict do nothing;

INSERT INTO setting_general(created_by, created_at, name, value, additional_data)
VALUES ('System', now(), 'SMS_OTP_TEMPLATE', ' رمز عبور یکبار مصرف: %s', 'الگو پیام رمز')
on conflict do nothing;

INSERT INTO setting_general(created_by, created_at, name, value, additional_data)
VALUES ('System', now(), 'MOBILE_FOR_GOT_ALERT', '09124162337', 'شماره همراه دریافت خطا')
on conflict do nothing;

INSERT INTO setting_general(created_by, created_at, name, value, additional_data)
VALUES ('System', now(), 'SMS_SEND_ALERT', 'false', 'ارسال پیام کوتاه در مانبتورینگ')
on conflict do nothing;

INSERT INTO setting_general(created_by, created_at, name, value, additional_data)
VALUES ('System', now(), 'MAX_REGISTER_EXPIRE_TIME_MINUTES', '5', 'حداکثر زمان مجاز ثبت نام')
on conflict do nothing;

INSERT INTO limitation_general(created_by, created_at, name, value, additional_data)
VALUES ('System', now(), 'ENABLE_CASH_IN', 'false', 'قابلیت شارژ حساب')
on conflict do nothing;

INSERT INTO limitation_general(created_by, created_at, name, value, additional_data)
VALUES ('System', now(), 'MIN_AMOUNT_CASH_IN', '1000', 'حداقل مبلغ شارژ کیف پول')
on conflict do nothing;

INSERT INTO limitation_general(created_by, created_at, name, value, additional_data)
VALUES ('System', now(), 'MAX_AMOUNT_CASH_IN', '1000000', 'حداکثر مبلغ شارژ کیف پول')
on conflict do nothing;

INSERT INTO limitation_general(created_by, created_at, name, value, additional_data)
VALUES ('System', now(), 'MAX_WALLET_AMOUNT_DAILY_CASH_IN', '10000000', 'حداکثر مبالغ شارژ کیف پول در روز')
on conflict do nothing;

INSERT INTO limitation_general(created_by, created_at, name, value, additional_data)
VALUES ('System', now(), 'MAX_WALLET_BALANCE', '1000000', 'حداکثر مانده کیف پول')
on conflict do nothing;

INSERT into request_type(created_by, created_at, name, fa_name, display)
VALUES ('System', now(),'cash_in','شارژ کیف پول',1);

INSERT into request_type(created_by, created_at, name, fa_name, display)
VALUES ('System', now(),'cash_out','برداشت',1);

INSERT into request_type(created_by, created_at, name, fa_name, display)
VALUES ('System', now(),'buy','خرید',1);

INSERT into request_type(created_by, created_at, name, fa_name, display)
VALUES ('System', now(),'sell','فروش',1);



CREATE INDEX ON channel_access_token (channel_id);
CREATE INDEX ON channel_block (channel_id);
CREATE UNIQUE INDEX idx_unique_national_code_wallet_type ON wallet (national_code, wallet_type_id);
insert into channel(created_by, created_at, firstname, lastname, username, mobile, password, trust, sign, public_key,
                    ip, status, account, check_shahkar)
values ('Systen', now(), 'Mahdi', 'admin', 'admin', '09124162337',
        '$2a$10$U5lecEunX.HBU.MBVLUV8OvwCrgGDJtaKVgGA5hzgwfsfTV8GD8TK', 1, 0, '', '0.0.0.0/0', 1,
        '1234567890', '0');

insert into resource(created_by, created_at, name, fa_name, display)
values ('system', 'now()', 'WALLET_CREATE', 'ایجاد کیف پول', 1);
insert into resource(created_by, created_at, name, fa_name, display)
values ('system', 'now()', 'WALLET_DEACTIVATE', 'غیر فعال کیف پول', 1);
insert into resource(created_by, created_at, name, fa_name, display)
values ('system', 'now()', 'WALLET_DELETE', 'حذف کیف پول', 1);
insert into resource(created_by, created_at, name, fa_name, display)
values ('system', 'now()', 'WALLET_ACTIVE', 'فعال کردن کیف پول', 1);
insert into resource(created_by, created_at, name, fa_name, display)
values ('system', 'now()', 'WALLET_INFO', 'دریافت اطلاعات کیف پول', 1);
insert into resource(created_by, created_at, name, fa_name, display)
values ('system', 'now()', 'GENERATE_UNIQUE_IDENTIFIER', 'تولید شناسه یکتا', 1);
insert into resource(created_by, created_at, name, fa_name, display)
values ('system', 'now()', 'CASH_IN', 'واریز وجه', 1);
insert into resource(created_by, created_at, name, fa_name, display)
values ('system', 'now()', 'BUY', 'خرید', 1);
insert into resource(created_by, created_at, name, fa_name, display)
values ('system', 'now()', 'SELL', 'فروش', 1);
insert into resource(created_by, created_at, name, fa_name, display)
values ('system', 'now()', 'LOGOUT', 'خروج', 1);