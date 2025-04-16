CREATE TABLE transaction_part_0000000001 PARTITION OF transaction_part FOR VALUES FROM (1) TO (100000);

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

insert into channel_role(created_by, created_at, role_id, channel_id) values ('system',now(),(select id from role_ where name='WEB_PROFILE'), (select id from channel where username='admin'))