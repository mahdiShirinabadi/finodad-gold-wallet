package com.melli.hub;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ChannelException extends Exception {

    private final int resultCode;
    private final int httpStatusCode;
    private final String channelMessage;
    private final String completeResponse;

    public ChannelException(String message, int status, String channelMessage, int httpStatusCode, String completeResponse) {
        super(message);
        this.resultCode = status;
        this.channelMessage = channelMessage;
        this.httpStatusCode = httpStatusCode;
        this.completeResponse = completeResponse;
    }
}
