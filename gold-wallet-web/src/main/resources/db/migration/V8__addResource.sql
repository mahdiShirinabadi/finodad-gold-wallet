insert into resource(created_by, created_at, name, fa_name, display)
values ('System', now(), 'ROLE_MANAGE', 'مدیریت نقش ها', 1)
    on conflict do nothing;

insert into resource(created_by, created_at, name, fa_name, display)
values ('System', now(), 'RESOURCE_MANAGE', 'مدیریت منابع', 1)
    on conflict do nothing;

insert into resource(created_by, created_at, name, fa_name, display)
values ('System', now(), 'MERCHANT_MANAGE', 'مدیریت پذیرنده ها', 1)
on conflict do nothing;


insert into resource(created_by, created_at, name, fa_name, display)
values ('System', now(), 'PANEL_CHANNEL_LIST', 'مدیریت کانال ها', 1)
    on conflict do nothing;

insert into resource(created_by, created_at, name, fa_name, display)
values ('System', now(), 'CHANNEL_MANAGE', 'مدیریت کانال ها', 1)
on conflict do nothing;

insert into resource(created_by, created_at, name, fa_name, display)
values ('System', now(), 'LIMITATION_MANAGE', 'مدیریت کانال ها', 1)
on conflict do nothing;

insert into resource(created_by, created_at, name, fa_name, display)
values ('System', now(), 'STATEMENT', 'صورتحساب', 1)
on conflict do nothing;

insert into role_resource(created_by, created_at, role_id, resource_id)
values ('system', 'now()', (select id from role_ where name='WEB_PROFILE'), (select id from resource where name='ROLE_MANAGE'));

insert into role_resource(created_by, created_at, role_id, resource_id)
values ('system', 'now()', (select id from role_ where name='WEB_PROFILE'), (select id from resource where name='RESOURCE_MANAGE'));

insert into role_resource(created_by, created_at, role_id, resource_id)
values ('system', 'now()', (select id from role_ where name='WEB_PROFILE'), (select id from resource where name='CHANNEL_MANAGE'));

insert into role_resource(created_by, created_at, role_id, resource_id)
values ('system', 'now()', (select id from role_ where name='WEB_PROFILE'), (select id from resource where name='MERCHANT_MANAGE'));

insert into role_resource(created_by, created_at, role_id, resource_id)
values ('system', 'now()', (select id from role_ where name='WEB_PROFILE'), (select id from resource where name='LIMITATION_MANAGE'));

insert into role_resource(created_by, created_at, role_id, resource_id)
values ('system', 'now()', (select id from role_ where name='WEB_PROFILE'), (select id from resource where name='STATEMENT'));

insert into role_resource(created_by, created_at, role_id, resource_id)
values ('system', 'now()', (select id from role_ where name='WEB_PROFILE'), (select id from resource where name='BUY_DIRECT'));

insert into role_resource(created_by, created_at, role_id, resource_id)
values ('system', 'now()', (select id from role_ where name='WEB_PROFILE'), (select id from resource where name='CASH_OUT'));