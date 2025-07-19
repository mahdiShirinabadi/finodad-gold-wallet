CREATE TABLE if not exists transaction_part_0000000001 PARTITION OF transaction_part FOR VALUES FROM (1) TO (100000);

insert into resource(created_by, created_at, name, fa_name, display)
values ('system', 'now()', 'BUY_DIRECT', 'لیست پذیرنده ها', 1)
    on conflict do nothing;


insert into role_resource(created_by, created_at, role_id, resource_id)
values ('system', 'now()', (select id from role_ where name='WEB_PROFILE'), (select id from resource where name='WALLET_CREATE'));
insert into role_resource(created_by, created_at, role_id, resource_id)
values ('system', 'now()', (select id from role_ where name='WEB_PROFILE'), (select id from resource where name='WALLET_DEACTIVATE'));
insert into role_resource(created_by, created_at, role_id, resource_id)
values ('system', 'now()', (select id from role_ where name='WEB_PROFILE'), (select id from resource where name='WALLET_DELETE'));
insert into role_resource(created_by, created_at, role_id, resource_id)
values ('system', 'now()', (select id from role_ where name='WEB_PROFILE'), (select id from resource where name='WALLET_ACTIVE'));
insert into role_resource(created_by, created_at, role_id, resource_id)
values ('system', 'now()', (select id from role_ where name='WEB_PROFILE'), (select id from resource where name='WALLET_INFO'));
insert into role_resource(created_by, created_at, role_id, resource_id)
values ('system', 'now()', (select id from role_ where name='WEB_PROFILE'), (select id from resource where name='GENERATE_UNIQUE_IDENTIFIER'));
insert into role_resource(created_by, created_at, role_id, resource_id)
values ('system', 'now()', (select id from role_ where name='WEB_PROFILE'), (select id from resource where name='CASH_IN'));
insert into role_resource(created_by, created_at, role_id, resource_id)
values ('system', 'now()', (select id from role_ where name='WEB_PROFILE'), (select id from resource where name='BUY'));
insert into role_resource(created_by, created_at, role_id, resource_id)
values ('system', 'now()', (select id from role_ where name='WEB_PROFILE'), (select id from resource where name='SELL'));
insert into role_resource(created_by, created_at, role_id, resource_id)
values ('system', 'now()', (select id from role_ where name='WEB_PROFILE'), (select id from resource where name='LOGOUT'));
insert into role_resource(created_by, created_at, role_id, resource_id)
values ('system', 'now()', (select id from role_ where name='WEB_PROFILE'), (select id from resource where name='GENERATE_PURCHASE_UNIQUE_IDENTIFIER'));
insert into role_resource(created_by, created_at, role_id, resource_id)
values ('system', 'now()', (select id from role_ where name='WEB_PROFILE'), (select id from resource where name='GENERATE_CASH_IN_UNIQUE_IDENTIFIER'));
insert into role_resource(created_by, created_at, role_id, resource_id)
values ('system', 'now()', (select id from role_ where name='WEB_PROFILE'), (select id from resource where name='MERCHANT_LIST'));
insert into role_resource(created_by, created_at, role_id, resource_id)
values ('system', 'now()', (select id from role_ where name='WEB_PROFILE'), (select id from resource where name='BUY_DIRECT'));

insert into channel_role(created_by, created_at, role_id, channel_id) values ('system',now(),(select id from role_ where name='WEB_PROFILE'), (select id from channel where username='admin'));

-- create merchant
insert into wallet(mobile, national_code, description, owner_channel_id, wallet_type_id, wallet_level_id, status,
                   created_at, created_by)
values ('09124162337', '1111111111', 'merchant',
        (select id from channel where username = 'admin'),
        (select id from wallet_type where name = 'MERCHANT'),
        (select id from wallet_level where name = 'BRONZE'), 'ACTIVE', now(), 'System');
insert into wallet_account(created_by, created_at, wallet_id, wallet_account_type_id, wallet_account_currency_id,
                           status)
values ('System', now(), (select id from wallet where national_code = '1111111111'),
        (select id from wallet_account_type where name = 'NORMAL'),
        (select id from wallet_account_currency where name = 'GOLD'), 'ACTIVE');


update wallet_account
set account_number=LPAD((select id
                         from wallet_account
                         where wallet_account_type_id = (select id from wallet_account_type where name = 'NORMAL')
                           and wallet_account_currency_id = (select id from wallet_account_currency where name = 'GOLD')
                           and wallet_id = (select id
                                            from wallet
                                            where national_code = '1111111111'
                                              and wallet.description = 'merchant'))::text, 8, '0')
where wallet_id = (select id from wallet where national_code = '1111111111' and wallet.description = 'merchant') and wallet_account_currency_id = (select id from wallet_account_currency where name = 'GOLD');


insert into wallet_account(created_by, created_at, wallet_id, wallet_account_type_id, wallet_account_currency_id,
                           status)
values ('System', now(), (select id from wallet where national_code = '1111111111'),
        (select id from wallet_account_type where name = 'NORMAL'),
        (select id from wallet_account_currency where name = 'RIAL'), 'ACTIVE');
update wallet_account
set account_number=LPAD((select id
                         from wallet_account
                         where wallet_account_type_id = (select id from wallet_account_type where name = 'NORMAL')
                           and wallet_account_currency_id = (select id from wallet_account_currency where name = 'RIAL')
                           and wallet_id = (select id
                                            from wallet
                                            where national_code = '1111111111'
                                              and wallet.description = 'merchant'))::text, 8, '0')
where wallet_id = (select id from wallet where national_code = '1111111111' and wallet.description = 'merchant') and wallet_account_currency_id = (select id from wallet_account_currency where name = 'RIAL');

insert into merchant(created_by, created_at, name, mobile, national_code, economical_code, wallet_id, settlement_type,status)
values ('System',now(),'فروشگاه تست','09124162337','1111111111','123467980',
        (select id from wallet where national_code = '1111111111' and wallet.description = 'merchant'),1,1);

