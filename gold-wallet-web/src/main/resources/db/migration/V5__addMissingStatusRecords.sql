-- Add missing status records from StatusService.java
insert into status(created_by, created_at, code, persian_description) values ('admin',now(),'2','رمز عبور مطابقت ندارد') on conflict do nothing;
insert into status(created_by, created_at, code, persian_description) values ('admin',now(),'5','کد ملی یافت نشد') on conflict do nothing;
insert into status(created_by, created_at, code, persian_description) values ('admin',now(),'6','کد ملی قبلا ثبت شده است') on conflict do nothing;
insert into status(created_by, created_at, code, persian_description) values ('admin',now(),'10','پروفایل یافت نشد') on conflict do nothing;
insert into status(created_by, created_at, code, persian_description) values ('admin',now(),'11','زمان به‌روزرسانی رمز عبور منقضی شده است') on conflict do nothing;
insert into status(created_by, created_at, code, persian_description) values ('admin',now(),'19','کیف پول با شماره همراه دیگری وجود دارد') on conflict do nothing;
insert into status(created_by, created_at, code, persian_description) values ('admin',now(),'22','تعداد OTP بیشتر از حد مجاز است') on conflict do nothing;
insert into status(created_by, created_at, code, persian_description) values ('admin',now(),'46','حساب کیف پذیرنده یافت نشد') on conflict do nothing;
insert into status(created_by, created_at, code, persian_description) values ('admin',now(),'47','حساب کیف کانال یافت نشد') on conflict do nothing;
insert into status(created_by, created_at, code, persian_description) values ('admin',now(),'48','تنظیمات بیشتر از یک رکورد دارد') on conflict do nothing;
insert into status(created_by, created_at, code, persian_description) values ('admin',now(),'49','نام محدودیت قبلا وجود دارد') on conflict do nothing;
insert into status(created_by, created_at, code, persian_description) values ('admin',now(),'50','محدودیت یافت نشد') on conflict do nothing;
insert into status(created_by, created_at, code, persian_description) values ('admin',now(),'51','قیمت با شناسه یکتا مطابقت ندارد') on conflict do nothing;
insert into status(created_by, created_at, code, persian_description) values ('admin',now(),'52','شماره حساب با شناسه یکتا مطابقت ندارد') on conflict do nothing;
insert into status(created_by, created_at, code, persian_description) values ('admin',now(),'53','خرید از محدودیت مبلغ روزانه فراتر رفته است') on conflict do nothing;
insert into status(created_by, created_at, code, persian_description) values ('admin',now(),'54','خرید از محدودیت تعداد روزانه فراتر رفته است') on conflict do nothing;
insert into status(created_by, created_at, code, persian_description) values ('admin',now(),'55','خرید از محدودیت مبلغ ماهانه فراتر رفته است') on conflict do nothing;
insert into status(created_by, created_at, code, persian_description) values ('admin',now(),'56','خرید از محدودیت تعداد ماهانه فراتر رفته است') on conflict do nothing;
insert into status(created_by, created_at, code, persian_description) values ('admin',now(),'57','واحد ارز کمیسیون معتبر نیست') on conflict do nothing;
insert into status(created_by, created_at, code, persian_description) values ('admin',now(),'58','تعداد کمتر از حد مجاز است') on conflict do nothing;
insert into status(created_by, created_at, code, persian_description) values ('admin',now(),'59','تعداد بیشتر از حد مجاز است') on conflict do nothing;
insert into status(created_by, created_at, code, persian_description) values ('admin',now(),'60','فروش از محدودیت مبلغ ماهانه فراتر رفته است') on conflict do nothing;
insert into status(created_by, created_at, code, persian_description) values ('admin',now(),'61','فروش از محدودیت تعداد ماهانه فراتر رفته است') on conflict do nothing;
insert into status(created_by, created_at, code, persian_description) values ('admin',now(),'62','فروش از محدودیت مبلغ روزانه فراتر رفته است') on conflict do nothing;
insert into status(created_by, created_at, code, persian_description) values ('admin',now(),'63','فروش از محدودیت تعداد روزانه فراتر رفته است') on conflict do nothing;
insert into status(created_by, created_at, code, persian_description) values ('admin',now(),'64','حساب اجازه برداشت وجه را ندارد') on conflict do nothing;
insert into status(created_by, created_at, code, persian_description) values ('admin',now(),'65','برداشت وجه از محدودیت تعداد ماهانه فراتر رفته است') on conflict do nothing;
insert into status(created_by, created_at, code, persian_description) values ('admin',now(),'66','برداشت وجه از محدودیت مبلغ روزانه فراتر رفته است') on conflict do nothing;
insert into status(created_by, created_at, code, persian_description) values ('admin',now(),'67','سطح کیف پول یافت نشد') on conflict do nothing;
insert into status(created_by, created_at, code, persian_description) values ('admin',now(),'68','موجودی پذیرنده کافی نیست') on conflict do nothing;
insert into status(created_by, created_at, code, persian_description) values ('admin',now(),'998','زمان انتظار به پایان رسیده است') on conflict do nothing;

-- Add new request types for merchant balance operations
INSERT into request_type(created_by, created_at, name, fa_name, display)
VALUES ('System', now(), 'merchant_increase_balance', 'افزایش مانده پذیرنده', 1)
on conflict do nothing;

INSERT into request_type(created_by, created_at, name, fa_name, display)
VALUES ('System', now(), 'merchant_decrease_balance', 'کاهش مانده پذیرنده', 1)
on conflict do nothing;

-- Add new resources for merchant balance operations
insert into resource(created_by, created_at, name, fa_name, display)
values ('System', now(), 'MERCHANT_INCREASE_BALANCE', 'افزایش مانده پذیرنده', 1)
on conflict do nothing;

insert into resource(created_by, created_at, name, fa_name, display)
values ('System', now(), 'MERCHANT_DECREASE_BALANCE', 'کاهش مانده پذیرنده', 1)
on conflict do nothing;
