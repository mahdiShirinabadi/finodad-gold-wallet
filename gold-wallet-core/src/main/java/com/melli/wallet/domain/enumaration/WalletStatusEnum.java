package com.melli.wallet.domain.enumaration;

import com.melli.wallet.util.StringUtils;
import lombok.Getter;

@Getter
public enum WalletStatusEnum {
    ACTIVE("ACTIVE", "فعال"),
    DISABLE("DISABLE", "غیرفعال"),
    DELETED("DELETED", "حذف شده");

    private final String text;
    private final String persianDescription;

    WalletStatusEnum(final String text, final String persianDescription) {
        this.text = text;
        this.persianDescription = persianDescription;
    }

    public static String getPersianDescription(String text) {
        if (!StringUtils.hasText(text)) {
            return "";
        }
        return switch (text) {
            case "ACTIVE" -> ACTIVE.persianDescription;
            case "DISABLE" -> DISABLE.persianDescription;
            case "DELETED" -> DELETED.persianDescription;
            default -> "";
        };
    }

    @Override
    public String toString() {
        return text;
    }
}
