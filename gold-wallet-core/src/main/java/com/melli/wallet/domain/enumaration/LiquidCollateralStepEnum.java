package com.melli.wallet.domain.enumaration;

/**
 * Class Name: TransactionTypeEnum
 * Author: Mahdi Shirinabadi
 * Date: 1/15/2025
 */
public enum LiquidCollateralStepEnum {

    CREATE("CREATE"),
    UNBLOCK("UNBLOCK"),
    SELL("SELL"),
    CASH_OUT("CASH_OUT"),
    ACTIVE_WALLET("ACTIVE_WALLET");

    private final String text;

    LiquidCollateralStepEnum(final String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return text;
    }
}
