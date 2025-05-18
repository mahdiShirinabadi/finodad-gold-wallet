package com.melli.wallet.domain.enumaration;

/**
 * Class Name: TransactionTypeEnum
 * Author: Mahdi Shirinabadi
 * Date: 1/15/2025
 */
public enum TransactionTypeEnum {

    BUY("BUY"),
    SELL("SELL");

    private final String text;

    TransactionTypeEnum(final String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return text;
    }
}
