package com.melli.wallet.domain.enumaration;

/**
 * Class Name: TransactionTypeEnum
 * Author: Mahdi Shirinabadi
 * Date: 1/15/2025
 */
public enum CollateralStatusEnum {

    CREATE("CREATE"),
    RELEASE("RELEASE");

    private final String text;

    CollateralStatusEnum(final String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return text;
    }
}
