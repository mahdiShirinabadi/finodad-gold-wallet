-- Add missing limitation_general records
-- Cash Out limitations
INSERT INTO limitation_general(created_by, created_at, name, value, additional_data)
VALUES ('System', now(), 'MIN_AMOUNT_CASH_OUT', '1000', 'حداقل مبلغ برداشت از کیف پول')
on conflict do nothing;

INSERT INTO limitation_general(created_by, created_at, name, value, additional_data)
VALUES ('System', now(), 'MAX_AMOUNT_CASH_OUT', '1000000', 'حداکثر مبلغ برداشت از کیف پول')
on conflict do nothing;

INSERT INTO limitation_general(created_by, created_at, name, value, additional_data)
VALUES ('System', now(), 'MAX_WALLET_AMOUNT_DAILY_CASH_OUT', '10000000', 'حداکثر مبالغ برداشت از کیف پول در روز')
on conflict do nothing;

-- Physical Cash Out limitations
INSERT INTO limitation_general(created_by, created_at, name, value, additional_data)
VALUES ('System', now(), 'MIN_QUANTITY_PHYSICAL_CASH_OUT', '5', 'حداقل مبلغ برداشت فیزیکی از کیف پول')
on conflict do nothing;

INSERT INTO limitation_general(created_by, created_at, name, value, additional_data)
VALUES ('System', now(), 'MAX_QUANTITY_PHYSICAL_CASH_OUT', '5', 'حداکثر مبلغ برداشت فیزیکی از کیف پول')
on conflict do nothing;

INSERT INTO limitation_general(created_by, created_at, name, value, additional_data)
VALUES ('System', now(), 'MAX_WALLET_QUANTITY_DAILY_PHYSICAL_CASH_OUT', '50',
        'حداکثر مبلغ برداشت فیزیکی روزانه از کیف پول')
on conflict do nothing;

INSERT INTO limitation_general(created_by, created_at, name, value, additional_data)
VALUES ('System', now(), 'MAX_MONTHLY_QUANTITY_BUY', '1000',
        'حداکثر مقدار خرید در یک ماه')
on conflict do nothing;

INSERT INTO limitation_general(created_by, created_at, name, value, additional_data)
VALUES ('System', now(), 'MAX_DAILY_QUANTITY_BUY', '10',
        'حداکثر مقدار خرید در یک روز')
on conflict do nothing;

INSERT INTO limitation_general(created_by, created_at, name, value, additional_data)
VALUES ('System', now(), 'MIN_QUANTITY_BUY', '0.001',
        'حداقل مقدار خرید در هر تراکنش')
on conflict do nothing;

INSERT INTO limitation_general(created_by, created_at, name, value, additional_data)
VALUES ('System', now(), 'MAX_QUANTITY_BUY', '10',
        'حداکثر مقدار خرید در هر تراکنش')
on conflict do nothing;

INSERT INTO limitation_general(created_by, created_at, name, value, additional_data)
VALUES ('System', now(), 'ENABLE_PHYSICAL_CASH_OUT', 'false', 'فعال بودن تحویل حضوری')
on conflict do nothing;

-- Add missing setting_general records
INSERT INTO setting_general(created_by, created_at, name, value, additional_data)
VALUES ('System', now(), 'SETTLE_DEFAULT_PAGE', '0', 'صفحه پیش‌فرض برای صفحه‌بندی')
on conflict do nothing;

INSERT INTO setting_general(created_by, created_at, name, value, additional_data)
VALUES ('System', now(), 'SETTLE_DEFAULT_SIZE', '10', 'اندازه پیش‌فرض برای صفحه‌بندی')
on conflict do nothing;

INSERT INTO setting_general(created_by, created_at, name, value, additional_data)
VALUES ('System', now(), 'SETTLEMENT_BATCH', 'false', 'تسویه به صورت گروهی')
on conflict do nothing;


-- Physical Cash Out resource moved to V9 migration

-- Physical Cash Out status codes
INSERT INTO status (created_by, created_at, code, persian_description, additional_data)
VALUES ('System', now(), '69', 'حساب مجوز برداشت فیزیکی ندارد', '')
on conflict do nothing;

INSERT INTO status (created_by, created_at, code, persian_description, additional_data)
VALUES ('System', now(), '70', 'تعداد برداشت فیزیکی ماهانه از حد مجاز تجاوز کرده', '')
on conflict do nothing;

INSERT INTO status (created_by, created_at, code, persian_description, additional_data)
VALUES ('System', now(), '71', 'مبلغ برداشت فیزیکی روزانه از حد مجاز تجاوز کرده', '')
on conflict do nothing;

-- Physical Cash Out template
INSERT INTO template (created_by, created_at, name, value)
VALUES ('System', now(), 'physical_cash_out', 'برداشت فیزیکی وجه به مبلغ {amount} ریال')
on conflict do nothing;

CREATE TABLE if not exists physical_cash_out_request
(
    request_id        BIGINT NOT NULL REFERENCES request,
    quantity          NUMERIC(15, 5), -- quantity
    final_quantity    NUMERIC(15, 5), -- final quantity user got from branch!!!
    commission        NUMERIC(15, 5), -- commission
    wallet_account_id BIGINT NOT NULL REFERENCES wallet_account,
    rrn_id            BIGINT NOT NULL REFERENCES rrn,
    additional_data   VARCHAR(500)
);
create unique index rrn_id_physical_cash_out_request_unique_idx on physical_cash_out_request (rrn_id);

INSERT INTO limitation_general(created_by, created_at, name, value, additional_data)
VALUES ('System', now(), 'MONTHLY_VALIDATION_CHECK_SELL', 'false', 'چک کردن محدودیت ماهانه در فروش')
on conflict do nothing;

INSERT INTO limitation_general(created_by, created_at, name, value, additional_data)
VALUES ('System', now(), 'MONTHLY_VALIDATION_CHECK_BUY', 'false', 'چک کردن محدودیت ماهانه در خرید')
on conflict do nothing;

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

INSERT INTO limitation_general(created_by, created_at, name, value, additional_data)
VALUES ('System', now(), 'MAX_DAILY_QUANTITY_P2P', '5', 'حداکثر مبلغ انتقال روزانه از کیف پول')
on conflict do nothing;

INSERT INTO limitation_general(created_by, created_at, name, value, additional_data)
VALUES ('System', now(), 'MAX_DAILY_COUNT_P2P', '10', 'حداکثر تعداد انتقال روزانه از کیف پول')
on conflict do nothing;

INSERT INTO limitation_general(created_by, created_at, name, value, additional_data)
VALUES ('System', now(), 'MIN_QUANTITY_P2P', '0.001', 'حداقل مبلغ انتقال از کیف پول در یک تراکنش')
on conflict do nothing;

INSERT INTO limitation_general(created_by, created_at, name, value, additional_data)
VALUES ('System', now(), 'MAX_QUANTITY_P2P', '5', 'حداکثر مبلغ انتقال از کیف پول در یک تراکنش')
on conflict do nothing;

INSERT into request_type(created_by, created_at, name, fa_name, display)
VALUES ('System', now(), 'p2p', 'انتقال', 1);

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

insert into status(created_by, created_at, code, persian_description) values ('admin',now(),'-1','ایجاد شده');
insert into status(created_by, created_at, code, persian_description) values ('admin',now(),'0','موفق');
insert into status(created_by, created_at, code, persian_description) values ('admin',now(),'1','پارامترهای ارسالی معتیر نیست');
insert into status(created_by, created_at, code, persian_description) values ('admin',now(),'3','دسترسی مورد نظر یافت نشد');
insert into status(created_by, created_at, code, persian_description) values ('admin',now(),'4','نوع درخواست بافت نشد');
insert into status(created_by, created_at, code, persian_description) values ('admin',now(),'7','نام کاربری یا رمز عبور نادرست است');
insert into status(created_by, created_at, code, persian_description) values ('admin',now(),'8','کاربر به سرویس مورد نظر دسترسی ندارد');
insert into status(created_by, created_at, code, persian_description) values ('admin',now(),'9','توکن ارسالی معتبر نیست');
insert into status(created_by, created_at, code, persian_description) values ('admin',now(),'12','کانال مسدود شده است');
insert into status(created_by, created_at, code, persian_description) values ('admin',now(),'13','آی پی ارسالی معتبر نیست');
insert into status(created_by, created_at, code, persian_description) values ('admin',now(),'14','درخواستها بیشتر از حد مجاز میباشد');
insert into status(created_by, created_at, code, persian_description) values ('admin',now(),'15','وضعیت یافت نشد');
insert into status(created_by, created_at, code, persian_description) values ('admin',now(),'16','خطا در فراخوانی شاهکار');
insert into status(created_by, created_at, code, persian_description) values ('admin',now(),'17','عدم تطابق شماهر همراه و کد ملی ارسالی شده');
insert into status(created_by, created_at, code, persian_description) values ('admin',now(),'18','خطا در ارسال پیام کوتاه');
insert into status(created_by, created_at, code, persian_description) values ('admin',now(),'23','نام کانال تکراری است');
insert into status(created_by, created_at, code, persian_description) values ('admin',now(),'24','کانال یافت نشد');
insert into status(created_by, created_at, code, persian_description) values ('admin',now(),'25','کیف پول بافت نشد');
insert into status(created_by, created_at, code, persian_description) values ('admin',now(),'26','نوع واحد حساب کیف یافت نشد');
insert into status(created_by, created_at, code, persian_description) values ('admin',now(),'27','نوع حساب کیف یافت نشد');
insert into status(created_by, created_at, code, persian_description) values ('admin',now(),'28','نوع کیف یافت نشد');
insert into status(created_by, created_at, code, persian_description) values ('admin',now(),'29','امضا دیجیتال نامعتبر میباشد');
insert into status(created_by, created_at, code, persian_description) values ('admin',now(),'30','ایحاد کیف پول با خطا همراه است');
insert into status(created_by, created_at, code, persian_description) values ('admin',now(),'31','شناسه یکتا یافت نشد');
insert into status(created_by, created_at, code, persian_description) values ('admin',now(),'32','شناسه یکتا تکراری است');
insert into status(created_by, created_at, code, persian_description) values ('admin',now(),'33','شماره مرجع تکراری میباشد');
insert into status(created_by, created_at, code, persian_description) values ('admin',now(),'34','کیف فعال نیست');
insert into status(created_by, created_at, code, persian_description) values ('admin',now(),'35','حساب کیف یافت نشد');
insert into status(created_by, created_at, code, persian_description) values ('admin',now(),'36','حساب کیف فعال نیست');
insert into status(created_by, created_at, code, persian_description) values ('admin',now(),'37','مبلغ کمتر از حد مجاز');
insert into status(created_by, created_at, code, persian_description) values ('admin',now(),'38','تنظیمات مرتبط یافت نشد');
insert into status(created_by, created_at, code, persian_description) values ('admin',now(),'39','کیف اجازه افزایش موجودی را ندارد');
insert into status(created_by, created_at, code, persian_description) values ('admin',now(),'40','مبلغ بیشتر از حد مجاز');
insert into status(created_by, created_at, code, persian_description) values ('admin',now(),'41','مانده کیف بیشتر از حد مجاز است');
insert into status(created_by, created_at, code, persian_description) values ('admin',now(),'42','مبلغ بیشتر از حد مجاز روزانه است');
insert into status(created_by, created_at, code, persian_description) values ('admin',now(),'43','موجودی کافی نیست');
insert into status(created_by, created_at, code, persian_description) values ('admin',now(),'44','رکورد یافت نشد');
insert into status(created_by, created_at, code, persian_description) values ('admin',now(),'45','پذیرنده یافت نشد');
insert into status(created_by, created_at, code, persian_description) values ('admin',now(),'898','رفرش توکن به کاربر متعلق نیست');
insert into status(created_by, created_at, code, persian_description) values ('admin',now(),'899','رفرش توکن یافت نشد');
insert into status(created_by, created_at, code, persian_description) values ('admin',now(),'900','رفرش توکن منقضی شده است');
insert into status(created_by, created_at, code, persian_description) values ('admin',now(),'988','خطا در همگام سازی');
insert into status(created_by, created_at, code, persian_description) values ('admin',now(),'999','بروز خطای ناشناخته در اجرای سرویس');
-- Add missing status records from StatusService.java
insert into status(created_by, created_at, code, persian_description) values ('admin',now(),'2','رمز عبور مطابقت ندارد') on conflict do nothing;
insert into status(created_by, created_at, code, persian_description) values ('admin',now(),'5','کد ملی یافت نشد') on conflict do nothing;
insert into status(created_by, created_at, code, persian_description) values ('admin',now(),'6','کد ملی قبلا ثبت شده است') on conflict do nothing;
insert into status(created_by, created_at, code, persian_description) values ('admin',now(),'10','پروفایل یافت نشد') on conflict do nothing;
insert into status(created_by, created_at, code, persian_description) values ('admin',now(),'11','زمان به‌روزرسانی رمز عبور منقضی شده است') on conflict do nothing;
insert into status(created_by, created_at, code, persian_description) values ('admin',now(),'19','کیف پول با شماره همراه دیگری وجود دارد') on conflict do nothing;
insert into status(created_by, created_at, code, persian_description) values ('admin',now(),'22','تعداد OTP بیشتر از حد مجاز است') on conflict do nothing;
insert into status(created_by, created_at, code, persian_description) values ('admin',now(),'46','حساب کیف پذیرنده یافت نشد') on conflict do nothing;
insert into status(created_by, created_at, code, persian_description) values ('admin',now(),'47','حساب کیف کانال یافت نشد') on conflict do nothing;
insert into status(created_by, created_at, code, persian_description) values ('admin',now(),'48','تنظیمات بیشتر از یک رکورد دارد') on conflict do nothing;
insert into status(created_by, created_at, code, persian_description) values ('admin',now(),'49','نام محدودیت قبلا وجود دارد') on conflict do nothing;
insert into status(created_by, created_at, code, persian_description) values ('admin',now(),'50','محدودیت یافت نشد') on conflict do nothing;
insert into status(created_by, created_at, code, persian_description) values ('admin',now(),'51','قیمت با شناسه یکتا مطابقت ندارد') on conflict do nothing;
insert into status(created_by, created_at, code, persian_description) values ('admin',now(),'52','شماره حساب با شناسه یکتا مطابقت ندارد') on conflict do nothing;
insert into status(created_by, created_at, code, persian_description) values ('admin',now(),'53','خرید از محدودیت مبلغ روزانه فراتر رفته است') on conflict do nothing;
insert into status(created_by, created_at, code, persian_description) values ('admin',now(),'54','خرید از محدودیت تعداد روزانه فراتر رفته است') on conflict do nothing;
insert into status(created_by, created_at, code, persian_description) values ('admin',now(),'55','خرید از محدودیت مبلغ ماهانه فراتر رفته است') on conflict do nothing;
insert into status(created_by, created_at, code, persian_description) values ('admin',now(),'56','خرید از محدودیت تعداد ماهانه فراتر رفته است') on conflict do nothing;
insert into status(created_by, created_at, code, persian_description) values ('admin',now(),'57','واحد ارز کمیسیون معتبر نیست') on conflict do nothing;
insert into status(created_by, created_at, code, persian_description) values ('admin',now(),'58','تعداد کمتر از حد مجاز است') on conflict do nothing;
insert into status(created_by, created_at, code, persian_description) values ('admin',now(),'59','تعداد بیشتر از حد مجاز است') on conflict do nothing;
insert into status(created_by, created_at, code, persian_description) values ('admin',now(),'60','فروش از محدودیت مبلغ ماهانه فراتر رفته است') on conflict do nothing;
insert into status(created_by, created_at, code, persian_description) values ('admin',now(),'61','فروش از محدودیت تعداد ماهانه فراتر رفته است') on conflict do nothing;
insert into status(created_by, created_at, code, persian_description) values ('admin',now(),'62','فروش از محدودیت مبلغ روزانه فراتر رفته است') on conflict do nothing;
insert into status(created_by, created_at, code, persian_description) values ('admin',now(),'63','فروش از محدودیت تعداد روزانه فراتر رفته است') on conflict do nothing;
insert into status(created_by, created_at, code, persian_description) values ('admin',now(),'64','حساب اجازه برداشت وجه را ندارد') on conflict do nothing;
insert into status(created_by, created_at, code, persian_description) values ('admin',now(),'65','برداشت وجه از محدودیت تعداد ماهانه فراتر رفته است') on conflict do nothing;
insert into status(created_by, created_at, code, persian_description) values ('admin',now(),'66','برداشت وجه از محدودیت مبلغ روزانه فراتر رفته است') on conflict do nothing;
insert into status(created_by, created_at, code, persian_description) values ('admin',now(),'67','سطح کیف پول یافت نشد') on conflict do nothing;
insert into status(created_by, created_at, code, persian_description) values ('admin',now(),'68','موجودی پذیرنده کافی نیست') on conflict do nothing;
insert into status(created_by, created_at, code, persian_description) values ('admin',now(),'998','زمان انتظار به پایان رسیده است') on conflict do nothing;