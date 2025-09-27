package com.melli.wallet.domain.enumaration;

/**
 * Class Name: TransactionTypeEnum
 * Author: Mahdi Shirinabadi
 * Date: 1/15/2025
 */
public enum ReleaseCollateralTypeEnum {

    COMPLETE("COMPLETE"),
    PARTIAL("PARTIAL");

    private final String text;

    ReleaseCollateralTypeEnum(final String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return text;
    }
}
