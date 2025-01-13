package com.melli.hub.domain.enumaration;

import lombok.Getter;

@Getter
public enum ChannelStatusEnum {
    ACTIVE(1),
    DEACTIVATED(2),
    BLOCKED(3);

    private final Integer value;

    ChannelStatusEnum(final Integer value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}
