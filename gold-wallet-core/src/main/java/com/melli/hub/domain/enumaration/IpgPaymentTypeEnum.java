package com.melli.hub.domain.enumaration;

import lombok.Getter;

@Getter
public enum IpgPaymentTypeEnum {
    FUND_ISSUE("FUND_ISSUE"),
    TRAVEL_INSURANCE_ISSUE("TRAVEL_INSURANCE_ISSUE"),
    FIRE_INSURANCE_ISSUE("TRAVEL_INSURANCE_ISSUE");

    private final String text;

    IpgPaymentTypeEnum(final String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return text;
    }
}
