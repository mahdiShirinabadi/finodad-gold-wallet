-- Insert and update status records with parameter placeholders for InternalServiceException messages
-- Parameters are formatted as ${1}, ${2}, etc. based on Map.ofEntries usage in code
-- Using INSERT ... ON CONFLICT DO UPDATE to replace old records without parameters

-- Code 37: AMOUNT_LESS_THAN_MIN - uses 2 params (amount, min)
INSERT INTO status(created_by, created_at, code, persian_description)
VALUES ('System', now(), '37', 'مبلغ کمتر از حد مجاز است- مبلغ وارد شده: [${1}]، حداقل مجاز: [${2}]')
ON CONFLICT (code) DO UPDATE SET persian_description = 'مبلغ کمتر از حد مجاز است- مبلغ وارد شده: [${1}]، حداقل مجاز: [${2}]';

-- Code 40: AMOUNT_BIGGER_THAN_MAX - uses 2 params (amount, max)
INSERT INTO status(created_by, created_at, code, persian_description)
VALUES ('System', now(), '40', 'مبلغ بیشتر از حد مجاز است- مبلغ وارد شده: [${1}]، حداکثر مجاز: [${2}]')
ON CONFLICT (code) DO UPDATE SET persian_description = 'مبلغ بیشتر از حد مجاز است- مبلغ وارد شده: [${1}]، حداکثر مجاز: [${2}]';

-- Code 41: BALANCE_MORE_THAN_STANDARD - uses 2 params (balance, max)
INSERT INTO status(created_by, created_at, code, persian_description)
VALUES ('System', now(), '41', 'موجودی از حد استاندارد بیشتر است- موجودی فعلی: [${1}]، حداکثر مجاز: [${2}]')
ON CONFLICT (code) DO UPDATE SET persian_description = 'موجودی از حد استاندارد بیشتر است- موجودی فعلی: [${1}]، حداکثر مجاز: [${2}]';

-- Code 42: WALLET_EXCEEDED_AMOUNT_LIMITATION - uses 2 params (current sum, max)
INSERT INTO status(created_by, created_at, code, persian_description)
VALUES ('System', now(), '42', 'مجموع واریز از حد مجاز فراتر رفته است- مجموع فعلی: [${1}]، حداکثر مجاز: [${2}]')
ON CONFLICT (code) DO UPDATE SET persian_description = 'مجموع واریز از حد مجاز فراتر رفته است- مجموع فعلی: [${1}]، حداکثر مجاز: [${2}]';

-- Code 53: BUY_EXCEEDED_QUANTITY_DAILY_LIMITATION - uses 2 params (current sum, max)
INSERT INTO status(created_by, created_at, code, persian_description)
VALUES ('System', now(), '53', 'خرید از محدودیت مبلغ روزانه فراتر رفته است- مجموع فعلی: [${1}]، حداکثر مجاز روزانه: [${2}]')
ON CONFLICT (code) DO UPDATE SET persian_description = 'خرید از محدودیت مبلغ روزانه فراتر رفته است- مجموع فعلی: [${1}]، حداکثر مجاز روزانه: [${2}]';

-- Code 54: BUY_EXCEEDED_COUNT_DAILY_LIMITATION - uses 2 params (current count, max)
INSERT INTO status(created_by, created_at, code, persian_description)
VALUES ('System', now(), '54', 'خرید از محدودیت تعداد روزانه فراتر رفته است- تعداد فعلی: [${1}]، حداکثر مجاز روزانه: [${2}]')
ON CONFLICT (code) DO UPDATE SET persian_description = 'خرید از محدودیت تعداد روزانه فراتر رفته است- تعداد فعلی: [${1}]، حداکثر مجاز روزانه: [${2}]';

-- Code 55: BUY_EXCEEDED_QUANTITY_MONTHLY_LIMITATION - uses 2 params (current sum, max)
INSERT INTO status(created_by, created_at, code, persian_description)
VALUES ('System', now(), '55', 'خرید از محدودیت مبلغ ماهانه فراتر رفته است- مجموع فعلی: [${1}]، حداکثر مجاز ماهانه: [${2}]')
ON CONFLICT (code) DO UPDATE SET persian_description = 'خرید از محدودیت مبلغ ماهانه فراتر رفته است- مجموع فعلی: [${1}]، حداکثر مجاز ماهانه: [${2}]';

-- Code 56: BUY_EXCEEDED_COUNT_MONTHLY_LIMITATION - uses 2 params (current count, max)
INSERT INTO status(created_by, created_at, code, persian_description)
VALUES ('System', now(), '56', 'خرید از محدودیت تعداد ماهانه فراتر رفته است- تعداد فعلی: [${1}]، حداکثر مجاز ماهانه: [${2}]')
ON CONFLICT (code) DO UPDATE SET persian_description = 'خرید از محدودیت تعداد ماهانه فراتر رفته است- تعداد فعلی: [${1}]، حداکثر مجاز ماهانه: [${2}]';

-- Code 58: QUANTITY_LESS_THAN_MIN - uses 2 params (quantity, min)
INSERT INTO status(created_by, created_at, code, persian_description)
VALUES ('System', now(), '58', 'تعداد کمتر از حد مجاز است- تعداد وارد شده: [${1}]، حداقل مجاز: [${2}]')
ON CONFLICT (code) DO UPDATE SET persian_description = 'تعداد کمتر از حد مجاز است- تعداد وارد شده: [${1}]، حداقل مجاز: [${2}]';

-- Code 59: QUANTITY_BIGGER_THAN_MAX - uses 2 params (quantity, max)
INSERT INTO status(created_by, created_at, code, persian_description)
VALUES ('System', now(), '59', 'تعداد بیشتر از حد مجاز است- تعداد وارد شده: [${1}]، حداکثر مجاز: [${2}]')
ON CONFLICT (code) DO UPDATE SET persian_description = 'تعداد بیشتر از حد مجاز است- تعداد وارد شده: [${1}]، حداکثر مجاز: [${2}]';

-- Code 60: EXCEEDED_AMOUNT_MONTHLY_LIMITATION - uses 2 params (current sum, max)
INSERT INTO status(created_by, created_at, code, persian_description)
VALUES ('System', now(), '60', 'مبلغ از محدودیت مبلغ ماهانه فراتر رفته است- مجموع فعلی: [${1}]، حداکثر مجاز ماهانه: [${2}]')
ON CONFLICT (code) DO UPDATE SET persian_description = 'مبلغ از محدودیت مبلغ ماهانه فراتر رفته است- مجموع فعلی: [${1}]، حداکثر مجاز ماهانه: [${2}]';

-- Code 61: EXCEEDED_COUNT_MONTHLY_LIMITATION - uses 2 params (current count, max)
INSERT INTO status(created_by, created_at, code, persian_description)
VALUES ('System', now(), '61', 'تعداد از محدودیت تعداد ماهانه فراتر رفته است- تعداد فعلی: [${1}]، حداکثر مجاز ماهانه: [${2}]')
ON CONFLICT (code) DO UPDATE SET persian_description = 'تعداد از محدودیت تعداد ماهانه فراتر رفته است- تعداد فعلی: [${1}]، حداکثر مجاز ماهانه: [${2}]';

-- Code 62: EXCEEDED_AMOUNT_DAILY_LIMITATION - uses 2 params (current sum, max)
INSERT INTO status(created_by, created_at, code, persian_description)
VALUES ('System', now(), '62', 'مجموع مبلغ بیشتر از حد مجاز- مجموع فعلی: [${1}]، حداکثر مبلغ روزانه: [${2}]')
ON CONFLICT (code) DO UPDATE SET persian_description = 'مجموع مبلغ بیشتر از حد مجاز- مجموع فعلی: [${1}]، حداکثر مبلغ روزانه: [${2}]';

-- Code 63: EXCEEDED_COUNT_DAILY_LIMITATION - uses 2 params (current count, max)
INSERT INTO status(created_by, created_at, code, persian_description)
VALUES ('System', now(), '63', 'تعداد بیشتر از حد مجاز- تعداد فعلی: [${1}]، حداکثر تعداد روزانه: [${2}]')
ON CONFLICT (code) DO UPDATE SET persian_description = 'تعداد بیشتر از حد مجاز- تعداد فعلی: [${1}]، حداکثر تعداد روزانه: [${2}]';

-- Code 65: CASHOUT_EXCEEDED_COUNT_MONTHLY_LIMITATION - uses 2 params (current count, max)
INSERT INTO status(created_by, created_at, code, persian_description)
VALUES ('System', now(), '65', 'برداشت وجه از محدودیت تعداد ماهانه فراتر رفته است- تعداد فعلی: [${1}]، حداکثر مجاز ماهانه: [${2}]')
ON CONFLICT (code) DO UPDATE SET persian_description = 'برداشت وجه از محدودیت تعداد ماهانه فراتر رفته است- تعداد فعلی: [${1}]، حداکثر مجاز ماهانه: [${2}]';

-- Code 66: CASHOUT_EXCEEDED_AMOUNT_DAILY_LIMITATION - uses 2 params (current sum, max)
INSERT INTO status(created_by, created_at, code, persian_description)
VALUES ('System', now(), '66', 'برداشت وجه از محدودیت مبلغ روزانه فراتر رفته است- مجموع فعلی: [${1}]، حداکثر مجاز روزانه: [${2}]')
ON CONFLICT (code) DO UPDATE SET persian_description = 'برداشت وجه از محدودیت مبلغ روزانه فراتر رفته است- مجموع فعلی: [${1}]، حداکثر مجاز روزانه: [${2}]';

-- Code 70: PHYSICAL_CASHOUT_EXCEEDED_COUNT_MONTHLY_LIMITATION - needs 2 params
INSERT INTO status(created_by, created_at, code, persian_description)
VALUES ('System', now(), '70', 'برداشت فیزیکی وجه از محدودیت تعداد ماهانه فراتر رفته است- تعداد فعلی: [${1}]، حداکثر مجاز ماهانه: [${2}]')
ON CONFLICT (code) DO UPDATE SET persian_description = 'برداشت فیزیکی وجه از محدودیت تعداد ماهانه فراتر رفته است- تعداد فعلی: [${1}]، حداکثر مجاز ماهانه: [${2}]';

-- Code 71: PHYSICAL_CASHOUT_EXCEEDED_AMOUNT_DAILY_LIMITATION - needs 2 params
INSERT INTO status(created_by, created_at, code, persian_description)
VALUES ('System', now(), '71', 'برداشت فیزیکی وجه از محدودیت مبلغ روزانه فراتر رفته است- مجموع فعلی: [${1}]، حداکثر مجاز روزانه: [${2}]')
ON CONFLICT (code) DO UPDATE SET persian_description = 'برداشت فیزیکی وجه از محدودیت مبلغ روزانه فراتر رفته است- مجموع فعلی: [${1}]، حداکثر مجاز روزانه: [${2}]';

CREATE TABLE shedlock(
                         name VARCHAR(64) NOT NULL,
                         lock_until TIMESTAMP NOT NULL,
                         locked_at TIMESTAMP NOT NULL,
                         locked_by VARCHAR(255) NOT NULL,
                         PRIMARY KEY (name)
);