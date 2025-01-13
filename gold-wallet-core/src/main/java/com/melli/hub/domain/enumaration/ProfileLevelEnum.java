package com.melli.hub.domain.enumaration;

import com.melli.hub.util.StringUtils;
import lombok.Getter;

@Getter
public enum ProfileLevelEnum {
    BORONZ("BORONZ","برنز"),
    SILVER("SILVER", "نقره"),
    GOLD("GOLD", "طلا"),
    PLATINUM("PLATINUM", "پلاتینیوم");

    private final String text;
    private final String persianDescription;

    ProfileLevelEnum(final String text, final String persianDescription) {
        this.text = text;
        this.persianDescription = persianDescription;
    }

    public static String getPersianDescription(String text) {
        if (!StringUtils.hasText(text)) {
            return "";
        }
        return switch (text) {
            case "BORONZ" -> BORONZ.persianDescription;
            case "SILVER" -> SILVER.persianDescription;
            case "GOLD" -> GOLD.persianDescription;
            case "PLATINUM" -> PLATINUM.persianDescription;
            default -> "";
        };
    }

    @Override
    public String toString() {
        return text;
    }
}
