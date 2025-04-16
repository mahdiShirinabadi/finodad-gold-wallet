package com.melli.wallet.slack.impl;

import com.melli.wallet.ChannelException;
import com.melli.wallet.slack.SlackChannelInterface;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile({"test"})
@Log4j2
public class SimpleSlackChannelInterfaceImplementation implements SlackChannelInterface {

    
    @Override
    public void sendMessage(String message) throws ChannelException {
        log.info("start call to slack channel with message ({})", message);
    }
}
