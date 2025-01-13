CREATE TABLE if not exists profile
(
    id            BIGSERIAL PRIMARY KEY,
    created_by    VARCHAR(200)                NOT NULL,
    updated_by    VARCHAR(200),
    created_at    TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at    TIMESTAMP WITHOUT TIME ZONE,
    first_name    VARCHAR(100),
    last_name     VARCHAR(100),
    national_code VARCHAR(10) UNIQUE,
    mobile        VARCHAR(20)                 NOT NULL,
    password      VARCHAR(100)                NOT NULL,
    birth_date    varchar(20),
    email         varchar(200),
    valid_ip      varchar(200),
    status        varchar(100),
    level         varchar(100),
    two_factor_authentication         boolean,
    end_time      TIMESTAMP WITHOUT TIME ZONE
);


CREATE TABLE if not exists role_
(
    id                  BIGSERIAL PRIMARY KEY,
    created_by          VARCHAR(200)                NOT NULL,
    updated_by          VARCHAR(200),
    created_at          TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at          TIMESTAMP WITHOUT TIME ZONE,
    name                VARCHAR(100) NOT NULL UNIQUE,
    persian_description VARCHAR(100),
    additional_data     VARCHAR(100),
    end_time            TIMESTAMP WITHOUT TIME ZONE
);

CREATE TABLE if not exists resource
(
    id         BIGSERIAL PRIMARY KEY,
    created_by VARCHAR(200)                NOT NULL,
    updated_by VARCHAR(200),
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITHOUT TIME ZONE,
    name       VARCHAR(100)                NOT NULL UNIQUE,
    fa_name    VARCHAR(100)                NOT NULL,
    display    INTEGER
);


CREATE TABLE if not exists request_type
(
    id         BIGSERIAL PRIMARY KEY,
    created_by VARCHAR(200)                NOT NULL,
    updated_by VARCHAR(200),
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITHOUT TIME ZONE,
    name       VARCHAR(100)                NOT NULL UNIQUE,
    fa_name    VARCHAR(100)                NOT NULL,
    display    INTEGER
);

CREATE TABLE if not exists profile_access_token
(
    id                        BIGSERIAL PRIMARY KEY,
    created_by                VARCHAR(200)                NOT NULL,
    updated_by                VARCHAR(200),
    created_at                TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at                TIMESTAMP WITHOUT TIME ZONE,
    profile_id                BIGINT                      NOT NULL REFERENCES profile,
    access_token              VARCHAR(400)                NOT NULL,
    refresh_token             VARCHAR(400)                NOT NULL,
    ip                        VARCHAR(500) DEFAULT NULL::CHARACTER VARYING,
    device_name               VARCHAR(400),
    additional_data           VARCHAR(400),
    access_token_expire_time  TIMESTAMP WITHOUT TIME ZONE,
    refresh_token_expire_time TIMESTAMP WITHOUT TIME ZONE,
    end_time                  TIMESTAMP WITHOUT TIME ZONE
);
CREATE INDEX ON profile_access_token (profile_id);


CREATE TABLE if not exists profile_block
(
    id               BIGSERIAL PRIMARY KEY,
    created_by       VARCHAR(200)                NOT NULL,
    updated_by       VARCHAR(200),
    created_at       TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at       TIMESTAMP WITHOUT TIME ZONE,
    profile_id       BIGINT                      NOT NULL REFERENCES profile,
    start_block_date TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    end_block_date   TIMESTAMP WITHOUT TIME ZONE,
    count_fail       INTEGER                     NOT NULL
);
CREATE INDEX ON profile_block (profile_id);


CREATE TABLE if not exists role_resource
(
    id            BIGSERIAL PRIMARY KEY,
    created_by    VARCHAR(200)                NOT NULL,
    updated_by    VARCHAR(200),
    created_at    TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at    TIMESTAMP WITHOUT TIME ZONE NULL,
    role_id       BIGINT                      NOT NULL REFERENCES role_,
    resource_id BIGINT                      NOT NULL REFERENCES resource
);

CREATE TABLE if not exists profile_role
(
    id         BIGSERIAL PRIMARY KEY,
    created_by VARCHAR(200)                NOT NULL,
    updated_by VARCHAR(200),
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITHOUT TIME ZONE NULL,
    role_id    BIGINT                      NOT NULL REFERENCES role_,
    profile_id BIGINT                      NOT NULL REFERENCES profile
);

CREATE TABLE if not exists setting
(
    id              BIGSERIAL PRIMARY KEY,
    created_by      VARCHAR(200)                NOT NULL,
    updated_by      VARCHAR(200),
    created_at      TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at      TIMESTAMP WITHOUT TIME ZONE,
    name            VARCHAR(100) NOT NULL UNIQUE,
    value           VARCHAR(100),
    pattern         VARCHAR(100),
    additional_data VARCHAR(100),
    end_time        TIMESTAMP WITHOUT TIME ZONE
);

CREATE TABLE if not exists status
(
    id                  BIGSERIAL PRIMARY KEY,
    created_by          VARCHAR(200)                NOT NULL,
    updated_by          VARCHAR(200),
    created_at          TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at          TIMESTAMP WITHOUT TIME ZONE,
    code                VARCHAR(100)                NOT NULL UNIQUE,
    persian_description VARCHAR(500),
    additional_data     VARCHAR(500)
);

CREATE TABLE if not exists version
(
    id              BIGSERIAL PRIMARY KEY,
    created_by      VARCHAR(200)                NOT NULL,
    updated_by      VARCHAR(200),
    created_at      TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at      TIMESTAMP WITHOUT TIME ZONE,
    version_number  VARCHAR(200)                NOT NULL,
    changes         VARCHAR(200)                NOT NULL,
    additional_data VARCHAR(200),
    active          BOOLEAN                     NOT NULL,
    end_time        TIMESTAMP WITHOUT TIME ZONE
);


create table if not exists verification_code
(
    id            bigserial                   not null primary key,
    created_by    varchar(200),
    updated_by    varchar(200),
    created_at    timestamp without time zone not null,
    updated_at    timestamp without time zone null,
    national_code varchar(100)                not null,
    code          varchar(100)                not null,
    expire_time   timestamp without time zone default null,
    status        int                         not null,
    type          varchar(100)                not null
);


create table if not exists temp_register_profile
(
    id            bigserial                   not null primary key,
    created_by    varchar(200),
    updated_by    varchar(200),
    created_at    timestamp without time zone not null,
    updated_at    timestamp without time zone null,
    national_code varchar(100)                not null,
    mobile        varchar(100)                not null,
    otp           varchar(100)                null,
    check_shahkar varchar(100)                null,
    step          varchar(100)                not null,
    ip            varchar(100)                not null,
    temp_uuid     varchar(100)                not null,
    expire_time   timestamp without time zone default null
);

CREATE TABLE if not exists shahkar_info
(
    id                    BIGSERIAL PRIMARY KEY,
    created_by            VARCHAR(200)                NOT NULL,
    updated_by            VARCHAR(200),
    created_at            TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at            TIMESTAMP WITHOUT TIME ZONE,
    national_code         VARCHAR(100),
    mobile                VARCHAR(100),
    channel_request_time  TIMESTAMP WITHOUT TIME ZONE,
    channel_response_time TIMESTAMP WITHOUT TIME ZONE,
    channel_response      TEXT,
    is_match              boolean
);

CREATE INDEX ON temp_register_profile (national_code);
CREATE INDEX ON temp_register_profile (temp_uuid);


insert into role_ (created_by, created_at, name, persian_description, additional_data)
values ('admin', now(), 'WEB_PROFILE', 'کاربر وب', '') on conflict do nothing;;

INSERT INTO setting(created_by, created_at, name, value, additional_data)
VALUES ('System', now(), 'MAX_WRONG_PASSWORD_FOR_PROFILE', '5', 'حداکثر تعداد رمز نادرست') on conflict do nothing;;

INSERT INTO setting(created_by, created_at, name, value, additional_data)
VALUES ('System', now(), 'DURATION_ACCESS_TOKEN_PROFILE', '600', 'زمان توکن پروفایل') on conflict do nothing;;

INSERT INTO setting(created_by, created_at, name, value, additional_data)
VALUES ('System', now(), 'DURATION_REFRESH_TOKEN_PROFILE', '86400', 'زمان توکن پروفایل') on conflict do nothing;;

INSERT INTO setting(created_by, created_at, name, value, additional_data)
VALUES ('System', now(), 'MAX_OTP_EXPIRE_TIME_MINUTES', '3', 'حداکثر زمان otp') on conflict do nothing;;

INSERT INTO setting(created_by, created_at, name, value, additional_data)
VALUES ('System', now(), 'LENGTH_OTP', '5', 'طول رمز') on conflict do nothing;;

INSERT INTO setting(created_by, created_at, name, value, additional_data)
VALUES ('System', now(), 'SMS_OTP_TEMPLATE', ' رمز عبور یکبار مصرف: %s', 'الگو پیام رمز') on conflict do nothing;;

INSERT INTO setting(created_by, created_at, name, value, additional_data)
VALUES ('System', now(), 'MOBILE_FOR_GOT_ALERT', '09124162337', 'شماره همراه دریافت خطا') on conflict do nothing;;

INSERT INTO setting(created_by, created_at, name, value, additional_data)
VALUES ('System', now(), 'SMS_SEND_ALERT', 'false', 'ارسال پیام کوتاه در مانبتورینگ') on conflict do nothing;;

INSERT INTO setting(created_by, created_at, name, value, additional_data)
VALUES ('System', now(), 'MAX_REGISTER_EXPIRE_TIME_MINUTES', '5', 'حداکثر زمان مجاز ثبت نام') on conflict do nothing;
