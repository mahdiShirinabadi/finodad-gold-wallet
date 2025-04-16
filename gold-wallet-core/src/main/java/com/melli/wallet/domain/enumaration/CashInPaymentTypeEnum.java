package com.melli.wallet.domain.enumaration;

import lombok.Getter;

@Getter
public enum CashInPaymentTypeEnum {
    ACCOUNT_TO_ACCOUNT("ACCOUNT_TO_ACCOUNT"),
    IPG("IPG"),
    MPG("MPG");

    private final String text;

    CashInPaymentTypeEnum(final String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return text;
    }
}
