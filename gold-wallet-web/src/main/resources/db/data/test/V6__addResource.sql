insert into role_resource(created_by, created_at, role_id, resource_id)
values ('system', 'now()', (select id from role_ where name='WEB_PROFILE'), (select id from resource where name='MERCHANT_BALANCE'));
insert into role_resource(created_by, created_at, role_id, resource_id)
values ('system', 'now()', (select id from role_ where name='WEB_PROFILE'), (select id from resource where name='MERCHANT_INCREASE_BALANCE'));
insert into role_resource(created_by, created_at, role_id, resource_id)
values ('system', 'now()', (select id from role_ where name='WEB_PROFILE'), (select id from resource where name='MERCHANT_DECREASE_BALANCE'));
insert into role_resource(created_by, created_at, role_id, resource_id)
values ('system', 'now()', (select id from role_ where name='WEB_PROFILE'), (select id from resource where name='CASH_OUT'));
