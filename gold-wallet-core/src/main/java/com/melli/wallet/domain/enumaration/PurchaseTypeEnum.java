package com.melli.wallet.domain.enumaration;

import lombok.Getter;

@Getter
public enum PurchaseTypeEnum {
    BUY("BUY"),
    SELL("SELL");

    private final String text;

    PurchaseTypeEnum(final String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return text;
    }
}
