package com.melli.hub.domain.enumaration;

import lombok.Getter;

@Getter
public enum FundInfoTypeEnum {
    FUND_NAV_INFO("FUND_NAV_INFO"),
    FUND_STATISTIC_INFO("FUND_STATISTIC_INFO");

    private final String text;

    FundInfoTypeEnum(final String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return text;
    }
}
