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

-- Add missing setting_general records
INSERT INTO setting_general(created_by, created_at, name, value, additional_data)
VALUES ('System', now(), 'SETTLE_DEFAULT_PAGE', '0', 'صفحه پیش‌فرض برای صفحه‌بندی')
on conflict do nothing;

INSERT INTO setting_general(created_by, created_at, name, value, additional_data)
VALUES ('System', now(), 'SETTLE_DEFAULT_SIZE', '10', 'اندازه پیش‌فرض برای صفحه‌بندی')
on conflict do nothing; 