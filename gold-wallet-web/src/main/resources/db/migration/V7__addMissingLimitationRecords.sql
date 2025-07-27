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
VALUES ('System', now(), 'MAX_DAILY_QUANTITY_BUY', '50',
        'حداکثر مقدار خرید در یک روز')
on conflict do nothing;

INSERT INTO limitation_general(created_by, created_at, name, value, additional_data)
VALUES ('System', now(), 'MIN_QUANTITY_BUY', '0.001',
        'حداقل مقدار خرید در هر تراکنش')
on conflict do nothing;

INSERT INTO limitation_general(created_by, created_at, name, value, additional_data)
VALUES ('System', now(), 'MAX_QUANTITY_BUY', '5',
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

-- Physical Cash Out request type
INSERT INTO request_type (created_by, created_at, name, fa_name, display)
VALUES ('System', now(), 'physical_cash_out', 'برداشت فیزیکی وجه', 1)
on conflict do nothing;

-- Physical Cash Out resource
INSERT INTO resource (created_by, created_at, name, fa_name, display)
VALUES ('System', now(), 'PHYSICAL_CASH_OUT', 'برداشت فیزیکی وجه', 1)
on conflict do nothing;

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