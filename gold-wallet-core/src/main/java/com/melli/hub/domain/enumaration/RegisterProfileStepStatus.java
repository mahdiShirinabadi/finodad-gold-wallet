package com.melli.hub.domain.enumaration;

import com.melli.hub.util.StringUtils;
import lombok.Getter;

@Getter
public enum RegisterProfileStepStatus {
    SEND_OTP("SEND_OTP", "ارسال otp"),
    VERIFY_OTP("VERIFY_OTP", "otp موفق"),
    SHAHKAR("SHAHKAR", "شاهکار"),
    FAIL_SHAHKAR("FAIL_SHAHKAR", "شاهکار ناموفق"),
    REGISTER("REGISTER", "ثبت نام");

    private final String text;
    private final String persianDescription;

    RegisterProfileStepStatus(final String text, final String persianDescription) {
        this.text = text;
        this.persianDescription = persianDescription;
    }

    public static String getPersianDescription(String text) {
        if (!StringUtils.hasText(text)) {
            return "";
        }
        return switch (text) {
            case "SEND_OTP" -> SEND_OTP.persianDescription;
            case "VERIFY_OTP" -> VERIFY_OTP.persianDescription;
            case "SHAHKAR" -> SHAHKAR.persianDescription;
            case "FAIL_SHAHKAR" -> FAIL_SHAHKAR.persianDescription;
            case "REGISTER" -> REGISTER.persianDescription;
            default -> "";
        };
    }

    @Override
    public String toString() {
        return text;
    }
}
