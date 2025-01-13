package com.melli.hub.domain.enumaration;

import com.melli.hub.util.StringUtils;
import lombok.Getter;

@Getter
public enum GiftCardStepStatus {
    INITIAL("INITIAL", "ایجاد شده"),
    USED("USED", "استفاده شده");

    private final String text;
    private final String persianDescription;

    GiftCardStepStatus(final String text, final String persianDescription) {
        this.text = text;
        this.persianDescription = persianDescription;
    }

    public static String getPersianDescription(String text) {
        if (!StringUtils.hasText(text)) {
            return "";
        }
        return switch (text) {
            case "INITIAL" -> INITIAL.persianDescription;
            case "USED" -> USED.persianDescription;
            default -> "";
        };
    }

    @Override
    public String toString() {
        return text;
    }
}
