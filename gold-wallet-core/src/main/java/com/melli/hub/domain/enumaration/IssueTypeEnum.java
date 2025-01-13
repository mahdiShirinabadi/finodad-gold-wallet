package com.melli.hub.domain.enumaration;

import lombok.Getter;

@Getter
public enum IssueTypeEnum {
    ACCOUNT_TO_ACCOUNT("ACCOUNT_TO_ACCOUNT"),
    BRANCH_ACCOUNT_TO_ACCOUNT("BRANCH_ACCOUNT_TO_ACCOUNT"),
    IPG("IPG"),
    MPG("MPG");

    private final String text;

    IssueTypeEnum(final String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return text;
    }
}
