package com.melli.wallet.domain.enumaration;

/**
 * Class Name: TransactionTypeEnum
 * Author: Mahdi Shirinabadi
 * Date: 1/15/2025
 */
public enum CashInIpgStepEnum {

    CREATE("CREATE"),
    GET_TOKEN("GET_TOKEN"),
    REDIRECT_IPG("REDIRECT_IPG"),
    CALL_BACK_FROM_PSP("CALL_BACK_FROM_PSP"),
    VERIFY("VERIFY"),
    REVERSE("REVERSE");

    private final String text;

    CashInIpgStepEnum(final String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return text;
    }
}
