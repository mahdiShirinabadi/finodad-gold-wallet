package com.melli.hub.domain.enumaration;

import lombok.Getter;

@Getter
public enum FundTypeEnum {
    ETF("ETF"),
    NonETF("NonETF");

    private final String text;

    FundTypeEnum(final String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return text;
    }
}
