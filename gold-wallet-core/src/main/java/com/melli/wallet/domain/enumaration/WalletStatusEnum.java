package com.melli.wallet.domain.enumaration;

import com.melli.wallet.util.CustomStringUtils;
import lombok.Getter;

@Getter
public enum WalletStatusEnum {
    ACTIVE("ACTIVE", "فعال"),
    DISABLE("DISABLE", "غیرفعال"),
    SUSPEND("SUSPEND", "معلق"),
    DELETED("DELETED", "حذف شده");

    private final String text;
    private final String persianDescription;

    WalletStatusEnum(final String text, final String persianDescription) {
        this.text = text;
        this.persianDescription = persianDescription;
    }

    public static String getPersianDescription(String text) {
        if (!CustomStringUtils.hasText(text)) {
            return "";
        }
        return switch (text) {
            case "ACTIVE" -> ACTIVE.persianDescription;
            case "DISABLE" -> DISABLE.persianDescription;
            case "DELETED" -> DELETED.persianDescription;
            case "SUSPEND" -> SUSPEND.persianDescription;
            default -> "";
        };
    }

    @Override
    public String toString() {
        return text;
    }
}
