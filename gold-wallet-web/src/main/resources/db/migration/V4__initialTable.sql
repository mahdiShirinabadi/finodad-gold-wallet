insert into resource(created_by, created_at, name, fa_name, display)
values ('system', 'now()', 'BUY_DIRECT', 'خرید مستقیم یدون شارژ', 1)
on conflict do nothing;

insert into resource(created_by, created_at, name, fa_name, display)
values ('system', 'now()', 'MERCHANT_BALANCE', 'مانده پذیرنده', 1)
    on conflict do nothing;