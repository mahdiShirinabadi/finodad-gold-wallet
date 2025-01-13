package com.melli.hub.domain.enumaration;

import com.melli.hub.util.StringUtils;
import lombok.Getter;

@Getter
public enum ProfileStatusEnum {
    REGISTER("REGISTER","ثبت نام شده"),
    BLOCK("BLOCK", "مسدود شده"),
    DELETED("DELETED", "حذف شده"),
    SUSPEND("SUSPEND", "معلق");

    private final String text;
    private final String persianDescription;

    ProfileStatusEnum(final String text, final String persianDescription) {
        this.text = text;
        this.persianDescription = persianDescription;
    }

    public static String getPersianDescription(String text) {
        if (!StringUtils.hasText(text)) {
            return "";
        }
        return switch (text) {
            case "REGISTER" -> REGISTER.persianDescription;
            case "BLOCK" -> BLOCK.persianDescription;
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
