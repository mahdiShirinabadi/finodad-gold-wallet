package com.melli.hub.domain.enumaration;

import lombok.Getter;

@Getter
public enum ErrorMessageTypeEnum {
    STATUS_TABLE("STATUS_TABLE"),
    CHANNEL_MESSAGE("CHANNEL_MESSAGE"),
    CUSTOM_MESSAGE("CUSTOM_MESSAGE");

    private final String code;

    ErrorMessageTypeEnum(final String text) {
        this.code = text;
    }

    @Override
    public String toString() {
        return String.valueOf(code);
    }
}
