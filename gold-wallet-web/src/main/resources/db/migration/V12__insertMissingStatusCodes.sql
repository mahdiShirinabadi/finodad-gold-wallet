-- =====================================================
-- V12__insertMissingStatusCodes.sql
-- Insert missing status codes from StatusRepositoryService
-- Only inserts status codes that are NOT already in previous migrations
-- =====================================================

-- Missing status codes from StatusRepositoryService that are not in V3, V5, or V11

-- Currency and Account Status Codes
INSERT INTO status(created_by, created_at, code, persian_description)
VALUES ('admin', NOW(), '93', 'ارز با حساب مطابقت ندارد')
ON CONFLICT (code) DO NOTHING;

-- Collateral Status Codes
INSERT INTO status(created_by, created_at, code, persian_description)
VALUES ('admin', NOW(), '94', 'حساب اجازه وثیقه را ندارد')
ON CONFLICT (code) DO NOTHING;

INSERT INTO status(created_by, created_at, code, persian_description)
VALUES ('admin', NOW(), '95', 'کد وثیقه یافت نشد')
ON CONFLICT (code) DO NOTHING;

INSERT INTO status(created_by, created_at, code, persian_description)
VALUES ('admin', NOW(), '96', 'مالک کد وثیقه یکسان است')
ON CONFLICT (code) DO NOTHING;

INSERT INTO status(created_by, created_at, code, persian_description)
VALUES ('admin', NOW(), '97', 'وثیقه قبلا آزاد شده است')
ON CONFLICT (code) DO NOTHING;

INSERT INTO status(created_by, created_at, code, persian_description)
VALUES ('admin', NOW(), '98', 'مقدار وثیقه یکسان نیست')
ON CONFLICT (code) DO NOTHING;

INSERT INTO status(created_by, created_at, code, persian_description)
VALUES ('admin', NOW(), '99', 'کد ملی وثیقه یکسان نیست')
ON CONFLICT (code) DO NOTHING;

INSERT INTO status(created_by, created_at, code, persian_description)
VALUES ('admin', NOW(), '100', 'مقدار وثیقه بیشتر از مقدار مسدود شده است')
ON CONFLICT (code) DO NOTHING;

INSERT INTO status(created_by, created_at, code, persian_description)
VALUES ('admin', NOW(), '101', 'مقدار مسدود شده کافی نیست')
ON CONFLICT (code) DO NOTHING;

INSERT INTO status(created_by, created_at, code, persian_description)
VALUES ('admin', NOW(), '102', 'حساب وثیقه یافت نشد')
ON CONFLICT (code) DO NOTHING;

INSERT INTO status(created_by, created_at, code, persian_description)
VALUES ('admin', NOW(), '103', 'وثیقه یافت نشد')
ON CONFLICT (code) DO NOTHING;

INSERT INTO status(created_by, created_at, code, persian_description)
VALUES ('admin', NOW(), '104', 'مرحله وثیقه باید مسدود باشد')
ON CONFLICT (code) DO NOTHING;

INSERT INTO status(created_by, created_at, code, persian_description)
VALUES ('admin', NOW(), '105', 'وثیقه قبلا مسدود شده است')
ON CONFLICT (code) DO NOTHING;

-- Physical Cash Out Status Codes
INSERT INTO status(created_by, created_at, code, persian_description)
VALUES ('admin', NOW(), '69', 'حساب اجازه برداشت فیزیکی وجه را ندارد')
ON CONFLICT (code) DO NOTHING;

INSERT INTO status(created_by, created_at, code, persian_description)
VALUES ('admin', NOW(), '70', 'برداشت فیزیکی وجه از محدودیت تعداد ماهانه فراتر رفته است')
ON CONFLICT (code) DO NOTHING;

INSERT INTO status(created_by, created_at, code, persian_description)
VALUES ('admin', NOW(), '71', 'برداشت فیزیکی وجه از محدودیت مبلغ روزانه فراتر رفته است')
ON CONFLICT (code) DO NOTHING;

-- General Error Codes (if not already present)
INSERT INTO status(created_by, created_at, code, persian_description)
VALUES ('admin', NOW(), '997', 'خطای داخلی')
ON CONFLICT (code) DO NOTHING;

-- =====================================================
-- Summary of inserted status codes:
-- 93: Currency not match with account
-- 94: Account don't permission for collateral
-- 95: Collateral code not found
-- 96: Owner collateral code same
-- 97: Collateral release before
-- 98: Collateral quantity not same
-- 99: Collateral national code not same
-- 100: Collateral quantity is bigger than block quantity
-- 101: Block amount not enough
-- 102: Collateral account not found
-- 103: Collateral not found
-- 104: Collateral step must be seize
-- 105: Collateral seize before
-- 69: Account don't permission for physical cash out
-- 70: Physical cashout exceeded count monthly limitation
-- 71: Physical cashout exceeded amount daily limitation
-- 997: Internal error
-- =====================================================
