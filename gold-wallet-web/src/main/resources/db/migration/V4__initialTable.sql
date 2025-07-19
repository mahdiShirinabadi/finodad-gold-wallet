insert into resource(created_by, created_at, name, fa_name, display)
values ('system', 'now()', 'BUY_DIRECT', 'لیست پذیرنده ها', 1)
on conflict do nothing;

