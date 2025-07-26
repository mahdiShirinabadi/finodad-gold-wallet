insert into role_resource(created_by, created_at, role_id, resource_id)
values ('system', 'now()', (select id from role_ where name='WEB_PROFILE'), (select id from resource where name='MERCHANT_BALANCE'));
insert into role_resource(created_by, created_at, role_id, resource_id)
values ('system', 'now()', (select id from role_ where name='WEB_PROFILE'), (select id from resource where name='MERCHANT_INCREASE_BALANCE'));
insert into role_resource(created_by, created_at, role_id, resource_id)
values ('system', 'now()', (select id from role_ where name='WEB_PROFILE'), (select id from resource where name='MERCHANT_DECREASE_BALANCE'));
insert into role_resource(created_by, created_at, role_id, resource_id)
values ('system', 'now()', (select id from role_ where name='WEB_PROFILE'), (select id from resource where name='CASH_OUT'));

-- Physical Cash Out resource
INSERT INTO resource (created_by, created_at, name, fa_name, display)
VALUES ('System', now(), 'PHYSICAL_CASH_OUT', 'برداشت فیزیکی وجه', 1)
    on conflict do nothing;

-- Physical Cash Out role-resource assignment
INSERT INTO role_resource(created_by, created_at, role_id, resource_id)
VALUES ('System', now(), (select id from role_ where name='WEB_PROFILE'), (select id from resource where name='PHYSICAL_CASH_OUT'))
    on conflict do nothing;
