package com.melli.wallet.domain.enumaration;

import com.melli.wallet.util.CustomStringUtils;

public enum SettlementStepEnum {
    INITIAL("INITIAL", "ایجاد شده"),
    IN_PROCESS("IN_PROCESS", "در حال انجام"),
    FAILED("FAILED", "ناموفق"),
    SUSPEND("SUSPEND", "لغو شده"),
    SUCCESS("SUCCESS", "موفق شده");

    private final String text;
    private final String persianDescription;

    SettlementStepEnum(final String text, final String persianDescription) {
        this.text = text;
        this.persianDescription = persianDescription;
    }

    public static String getPersianDescription(String text) {
        if (!CustomStringUtils.hasText(text)) {
            return "";
        }
        return switch (text) {
            case "INITIAL" -> INITIAL.persianDescription;
            case "IN_PROCESS" -> IN_PROCESS.persianDescription;
            case "FAILED" -> FAILED.persianDescription;
            case "SUSPEND" -> SUSPEND.persianDescription;
            case "SUCCESS" -> SUCCESS.persianDescription;
            default -> "";
        };
    }

    @Override
    public String toString() {
        return text;
    }
}
