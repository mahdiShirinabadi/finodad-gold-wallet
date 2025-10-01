-- Add new status records for panel management, gift cards, and other new features
-- Only adding status codes that are NOT already in previous migrations

-- Merchant Status Codes
insert into status(created_by, created_at, code, persian_description)
values ('admin', now(), '72', 'پذیرنده غیرفعال است')
on conflict do nothing;

-- Panel Role Management Status Codes
insert into status(created_by, created_at, code, persian_description)
values ('admin', now(), '73', 'نقش یافت نشد')
on conflict do nothing;
insert into status(created_by, created_at, code, persian_description)
values ('admin', now(), '74', 'نقش قبلا وجود دارد')
on conflict do nothing;
insert into status(created_by, created_at, code, persian_description)
values ('admin', now(), '75', 'نقش در حال استفاده است')
on conflict do nothing;
insert into status(created_by, created_at, code, persian_description)
values ('admin', now(), '76', 'نقش قبلا تخصیص داده شده است')
on conflict do nothing;
insert into status(created_by, created_at, code, persian_description)
values ('admin', now(), '77', 'نقش تخصیص داده نشده است')
on conflict do nothing;

-- Panel Resource Management Status Codes
insert into status(created_by, created_at, code, persian_description)
values ('admin', now(), '78', 'منبع یافت نشد')
on conflict do nothing;
insert into status(created_by, created_at, code, persian_description)
values ('admin', now(), '79', 'منبع قبلا وجود دارد')
on conflict do nothing;
insert into status(created_by, created_at, code, persian_description)
values ('admin', now(), '80', 'منبع در حال استفاده است')
on conflict do nothing;

-- Channel Status Codes
insert into status(created_by, created_at, code, persian_description)
values ('admin', now(), '81', 'کانال غیرفعال است')
on conflict do nothing;

-- Account and Transaction Status Codes
insert into status(created_by, created_at, code, persian_description)
values ('admin', now(), '82', 'خطا در ذخیره شماره حساب یکتا')
on conflict do nothing;
insert into status(created_by, created_at, code, persian_description)
values ('admin', now(), '83', 'حساب مبدا و مقصد یکسان است')
on conflict do nothing;
insert into status(created_by, created_at, code, persian_description)
values ('admin', now(), '84', 'ارز حساب مبدا و مقصد یکسان است')
on conflict do nothing;
insert into status(created_by, created_at, code, persian_description)
values ('admin', now(), '85', 'شماره حساب مقصد یافت نشد')
on conflict do nothing;

-- Stock and Commission Status Codes
insert into status(created_by, created_at, code, persian_description)
values ('admin', now(), '86', 'موجودی یافت نشد')
on conflict do nothing;
insert into status(created_by, created_at, code, persian_description)
values ('admin', now(), '87', 'کمیسیون بیشتر از مقدار است')
on conflict do nothing;

-- Gift Card Status Codes
insert into status(created_by, created_at, code, persian_description)
values ('admin', now(), '88', 'کارت هدیه یکتا نیست')
on conflict do nothing;
insert into status(created_by, created_at, code, persian_description)
values ('admin', now(), '89', 'حساب اجازه کارت هدیه را ندارد')
on conflict do nothing;
insert into status(created_by, created_at, code, persian_description)
values ('admin', now(), '90', 'کارت هدیه یافت نشد')
on conflict do nothing;
insert into status(created_by, created_at, code, persian_description)
values ('admin', now(), '91', 'کد ملی اجازه پرداخت کارت هدیه را ندارد')
on conflict do nothing;
insert into status(created_by, created_at, code, persian_description)
values ('admin', now(), '92', 'کارت هدیه منقضی شده است')
on conflict do nothing;

-- General Error Code
insert into status(created_by, created_at, code, persian_description)
values ('admin', now(), '99', 'خطای داخلی')
on conflict do nothing;

-- Add only new request types (giftCard is new)
insert into request_type(created_by, created_at, name, fa_name, display)
VALUES ('System', now(), 'gift_card', 'کارت هدیه', 1)
on conflict do nothing;

insert into request_type(created_by, created_at, name, fa_name, display)
VALUES ('System', now(), 'gift_card', 'کارت هدیه', 1)
on conflict do nothing;

insert into request_type(created_by, created_at, name, fa_name, display)
VALUES ('System', now(), 'gift_card', 'کارت هدیه', 1)
on conflict do nothing;

insert into request_type(created_by, created_at, name, fa_name, display)
VALUES ('System', now(), 'gift_card', 'کارت هدیه', 1)
on conflict do nothing;

insert into request_type(created_by, created_at, name, fa_name, display)
VALUES ('System', now(), 'gift_card', 'کارت هدیه', 1)
on conflict do nothing;

-- Add only new wallet account types (GIFT_CARD is new)
ALTER TABLE wallet_account_type
    ADD COLUMN display BOOLEAN DEFAULT true NOT NULL;
update wallet_account_type
set display = false
where name = 'RIAL';
insert into wallet_account_type(created_by, created_at, name, additional_data, description, display)
VALUES ('System', now(), 'GIFT_CARD', 'عادی', 'GIFT_CARD', false);

-- Add only new general settings (GIFT_CARD_RANDOM_STRING is new)
insert into setting_general(created_by, created_at, name, value, additional_data)
VALUES ('System', now(), 'GIFT_CARD_RANDOM_STRING', 'ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789%&=#@',
        'رشته تصادفی کارت هدیه')
on conflict do nothing;

-- Add Gift Card Limitation Settings
INSERT INTO limitation_general(created_by, created_at, name, value, additional_data)
VALUES ('System', now(), 'MAX_DAILY_QUANTITY_GIFT_CARD', '10.0', 'حداکثر مقدار روزانه کارت هدیه')
on conflict do nothing;

INSERT INTO limitation_general(created_by, created_at, name, value, additional_data)
VALUES ('System', now(), 'MAX_DAILY_COUNT_GIFT_CARD', '5', 'حداکثر تعداد روزانه کارت هدیه')
on conflict do nothing;

INSERT INTO limitation_general(created_by, created_at, name, value, additional_data)
VALUES ('System', now(), 'MAX_DAILY_QUANTITY_PAYMENT_GIFT_CARD', '10.0', 'حداکثر مقدار روزانه پرداخت کارت هدیه')
on conflict do nothing;

INSERT INTO limitation_general(created_by, created_at, name, value, additional_data)
VALUES ('System', now(), 'MAX_DAILY_COUNT_PAYMENT_GIFT_CARD', '3', 'حداکثر تعداد روزانه پرداخت کارت هدیه')
on conflict do nothing;

INSERT INTO limitation_general(created_by, created_at, name, value, additional_data)
VALUES ('System', now(), 'MIN_QUANTITY_GIFT_CARD', '0.001', 'حداقل مقدار کارت هدیه')
on conflict do nothing;

INSERT INTO limitation_general(created_by, created_at, name, value, additional_data)
VALUES ('System', now(), 'MAX_QUANTITY_GIFT_CARD', '2.0', 'حداکثر مقدار کارت هدیه')
on conflict do nothing;

INSERT INTO limitation_general(created_by, created_at, name, value, additional_data)
VALUES ('System', now(), 'ENABLE_GIFT_CARD', 'false', 'فعال/غیرفعال بودن کارت هدیه')
on conflict do nothing;

CREATE TABLE if not exists gift_card
(
    id                            BIGSERIAL PRIMARY KEY,
    created_by                    VARCHAR(200)                NOT NULL,
    updated_by                    VARCHAR(200),
    created_at                    TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at                    TIMESTAMP WITHOUT TIME ZONE,
    active_code                   varchar(100)                NOT NULL,
    rrn_id                        BIGINT                      NOT NULL REFERENCES rrn,
    wallet_account_id             BIGINT                      NOT NULL REFERENCES wallet_account,
    gift_wallet_account_id        BIGINT                      NOT NULL REFERENCES wallet_account,
    destination_wallet_account_id BIGINT REFERENCES wallet_account,
    wallet_account_currency_id    BIGINT REFERENCES wallet_account_currency,
    quantity                      NUMERIC(25, 5)              not null,
    commission                    NUMERIC(25, 5)              not null,
    status                        varchar(100)                not null,
    expire_at                     TIMESTAMP WITHOUT TIME ZONE,
    activated_at                  TIMESTAMP WITHOUT TIME ZONE,
    activated_by                  VARCHAR(200),
    nationalCode_by               CHAR(10)
);
create unique index rrn_id_gift_card_unique_idx on gift_card (active_code);


CREATE TABLE if not exists gift_card_payment_request
(
    request_id        BIGINT primary key NOT NULL REFERENCES request,
    quantity          NUMERIC(15, 5), -- Purchased quantity
    gift_card_id      BIGINT             NOT NULL REFERENCES gift_card,
    rrn_id            BIGINT             NOT NULL REFERENCES rrn,
    wallet_account_id BIGINT             NOT NULL REFERENCES wallet_account,
    additional_data   VARCHAR(500)
);
create unique index rrn_id_gift_card_payment_request_unique_idx on gift_card_payment_request (rrn_id);


CREATE TABLE if not exists create_collateral_request
(
    request_id             BIGINT NOT NULL REFERENCES request,
    wallet_account_id      BIGINT NOT NULL REFERENCES wallet_account,
    rrn_id                 BIGINT NOT NULL REFERENCES rrn,
    collateral_id                 BIGINT NOT NULL REFERENCES collateral,
    code                   varchar(200),
    additional_data        varchar(500),
    status                 varchar(100),
    quantity               NUMERIC(15, 5),
    final_block_quantity   NUMERIC(15, 5),
    commission             NUMERIC(15, 5)
);
create unique index rrn_id_create_collateral_request_unique_idx on create_collateral_request (rrn_id);


CREATE TABLE if not exists release_collateral_request
(
    request_id             BIGINT NOT NULL REFERENCES request,
    wallet_account_id      BIGINT NOT NULL REFERENCES wallet_account,
    rrn_id                 BIGINT NOT NULL REFERENCES rrn,
    create_collateral_request_id                 BIGINT NOT NULL REFERENCES request,
    additional_data        varchar(500),
    quantity               NUMERIC(15, 5),
    commission           NUMERIC(15, 5)
);
create unique index rrn_id_release_collateral_request_unique_idx on release_collateral_request (rrn_id);

INSERT INTO limitation_general(created_by, created_at, name, value, additional_data)
VALUES ('System', now(), 'ENABLE_COLLATERAL', 'false', 'فعال/غیرفعال بودن وثیقه')
on conflict do nothing;

insert into request_type(created_by, created_at, name, fa_name, display)
VALUES ('System', now(), 'create_collateral', 'ایجاد وثیقه', 1)
on conflict do nothing;

insert into request_type(created_by, created_at, name, fa_name, display)
VALUES ('System', now(), 'release_collateral', 'آزادسازی وثیقه', 1)
on conflict do nothing;

insert into request_type(created_by, created_at, name, fa_name, display)
VALUES ('System', now(), 'increase_collateral', 'آزادسازی وثیقه', 1)
on conflict do nothing;

insert into request_type(created_by, created_at, name, fa_name, display)
VALUES ('System', now(), 'seize_collateral', 'آزادسازی وثیقه', 1)
on conflict do nothing;

insert into request_type(created_by, created_at, name, fa_name, display)
VALUES ('System', now(), 'sell_collateral', 'آزادسازی وثیقه', 1)
on conflict do nothing;