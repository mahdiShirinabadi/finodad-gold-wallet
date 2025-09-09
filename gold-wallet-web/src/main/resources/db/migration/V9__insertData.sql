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