package com.melli.hub.domain.enumaration;

import com.melli.hub.util.StringUtils;

public enum StepStatus {
    SAVE_IN_DATABASE("SAVE_IN_DATABASE", "ذخیره شده"),
    BANK_INQUIRY("BANK_INQUIRY", "استعلام تراکنش از بانک"),
    SUCCESS_BANK_INQUIRY("SUCCESS_BANK_INQUIRY", "وجود تراکنش در بانک"),
    ERROR_BANK_INQUIRY("ERROR_BANK_INQUIRY", "خطا در استعلام تراکنش از بانک"),
    CHARGE_ACCOUNT("CHARGE_ACCOUNT","واریز وجه"),
    SUCCESS_CHARGE_ACCOUNT("SUCCESS_CHARGE_ACCOUNT","واریز وجه موفق لوده است"),
    INITIAL("INITIAL", "ایجاد شده"),
    FAILED("FAILED", "ناموفق"),
    CANCELED("CANCELED", "لغو شده"),
    CONFIRM("CONFIRM", "تایید شده"),
    NOT_FOUND("NOT_FOUND", "یافت نشد");

    private final String text;
    private final String persianDescription;

    StepStatus(final String text, final String persianDescription) {
        this.text = text;
        this.persianDescription = persianDescription;
    }

    public static String getPersianDescription(String text) {
        if (!StringUtils.hasText(text)) {
            return "";
        }
        return switch (text) {
            case "SAVE_IN_DATABASE", "SUCCESS_BANK_INQUIRY", "INITIAL", "CHARGE_ACCOUNT" -> INITIAL.persianDescription;
            case "ERROR_BANK_INQUIRY" -> ERROR_BANK_INQUIRY.persianDescription;
            case "FAILED" -> FAILED.persianDescription;
            case "CANCELED" -> CANCELED.persianDescription;
            case "CONFIRM" -> CONFIRM.persianDescription;
            case "NOT_FOUND" -> NOT_FOUND.persianDescription;
            default -> "";
        };
    }

    @Override
    public String toString() {
        return text;
    }
}
