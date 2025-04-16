package com.melli.wallet.sadad;

import com.melli.wallet.ChannelException;

import java.net.URISyntaxException;

public interface SadadChannelInterface {

    public static final String X_CLIENT_TOKEN = "X-CLIENT-TOKEN";
    int SUCCESSFUL = 0;
    int TIME_OUT = 998;
    int GENERAL_ERROR = 999;

    String SCOPE_FUND_TRANSFER = "money-transfer";
    String SCOPE_SHAHKAR = "icms-shahkar-inquiry";
    String SCOPE_STATEMENT = "svc-mgmt-mq-stmt-info";
    String SCOPE_SEND_SMS = "sadad-send-sms";

    String shahkar(String token ,String nationalCode, String mobile) throws ChannelException, URISyntaxException;
    String getToken(String clientId, String clientSecret, String scope) throws ChannelException, URISyntaxException;
    String sendSms(String token, String message, String mobile, int priority, int ttl) throws ChannelException, URISyntaxException;
}
