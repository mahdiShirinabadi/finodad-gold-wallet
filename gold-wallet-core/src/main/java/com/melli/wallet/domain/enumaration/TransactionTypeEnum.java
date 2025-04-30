package com.melli.wallet.domain.enumaration;

/**
 * Class Name: TransactionTypeEnum
 * Author: Mahdi Shirinabadi
 * Date: 1/15/2025
 */
public enum TransactionTypeEnum {

    CUSTOMER_BUY("BUY"),
    CUSTOMER_SELL("CUSTOMER_SELL");

    private final String text;

    TransactionTypeEnum(final String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return text;
    }
}
