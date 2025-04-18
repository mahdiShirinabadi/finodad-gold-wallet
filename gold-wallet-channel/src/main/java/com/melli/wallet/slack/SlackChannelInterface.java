package com.melli.wallet.slack;

import com.melli.wallet.ChannelException;

public interface SlackChannelInterface {

    int SUCCESS = 0;
    int GENERAL_ERROR = 999;
    int TIME_OUT = 998;

    void sendMessage(String message) throws ChannelException;
}
