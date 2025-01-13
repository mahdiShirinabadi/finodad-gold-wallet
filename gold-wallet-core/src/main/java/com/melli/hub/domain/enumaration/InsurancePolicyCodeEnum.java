package com.melli.hub.domain.enumaration;

import lombok.Getter;

@Getter
public enum InsurancePolicyCodeEnum {
    TRAVEL("TRAVEL"),
    FIRE("FIRE"),
    BADANE("BADANE");

    private final String text;

    InsurancePolicyCodeEnum(final String text) {
        this.text = text;
    }


    @Override
    public String toString() {
        return text;
    }
}
