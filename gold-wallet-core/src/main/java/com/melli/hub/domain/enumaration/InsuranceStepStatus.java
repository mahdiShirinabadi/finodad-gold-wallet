package com.melli.hub.domain.enumaration;

import com.melli.hub.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

public enum InsuranceStepStatus {
    CREATED("CREATED", "ایجاد شده"),
    FAILED_BANK_INQUIRY("FAILED_BANK_INQUIRY", "خطا در استعلام تراکنش از بانک"),
    FAILED_ISSUE("FAILED_ISSUE", "خطا در صدور پیش نویس بیمه"),
    FAILED_SETTLEMENT("FAILED_SETTLEMENT", "خطا در تسویه از صندوق"),
    FAILED("FAILED", "ناموفق"),
    SUCCESSFUL("SUCCESSFUL", "موفق");

    private static final Map<String, InsuranceStepStatus> persianDescriptionMap = new HashMap<>();

    static {
        for (InsuranceStepStatus status : InsuranceStepStatus.values()) {
            persianDescriptionMap.put(status.persianDescription, status);
        }
    }

    private final String text;
    private final String persianDescription;

    InsuranceStepStatus(final String text, final String persianDescription) {
        this.text = text;
        this.persianDescription = persianDescription;
    }

    public static String getPersianDescription(String text) {
        if (!StringUtils.hasText(text)) {
            return "";
        }
        return switch (text) {
            case "CREATED" -> CREATED.persianDescription;
            case "FAILED_BANK_INQUIRY" -> FAILED_BANK_INQUIRY.persianDescription;
            case "FAILED_ISSUE" -> FAILED_ISSUE.persianDescription;
            case "FAILED_SETTLEMENT" -> FAILED_SETTLEMENT.persianDescription;
            case "FAILED" -> FAILED.persianDescription;
            case "SUCCESSFUL" -> SUCCESSFUL.persianDescription;
            default -> "";
        };
    }

    public static InsuranceStepStatus fromPersianDescription(String persianDescription) {
        if (!StringUtils.hasText(persianDescription)) {
            return null;
        }
        return persianDescriptionMap.getOrDefault(persianDescription, null);
    }

    @Override
    public String toString() {
        return text;
    }
}
