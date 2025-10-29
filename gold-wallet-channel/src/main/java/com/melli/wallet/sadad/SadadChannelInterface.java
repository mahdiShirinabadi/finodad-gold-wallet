package com.melli.wallet.sadad;

import com.melli.wallet.ChannelException;

import java.net.URISyntaxException;
import java.util.Date;

public interface SadadChannelInterface {

    public static final String X_CLIENT_TOKEN = "X-CLIENT-TOKEN";
    int SUCCESSFUL = 0;
    int TIME_OUT = 998;
    int GENERAL_ERROR = 999;

    String SCOPE_FUND_TRANSFER = "money-transfer";
    String SCOPE_SHAHKAR = "icms-shahkar-inquiry";
    String SCOPE_STATEMENT = "svc-mgmt-mq-stmt-info";
    String SCOPE_SEND_SMS = "sadad-send-sms";
    String SCOPE_GET_MOBILE = "";
    String SCOPE_GET_BALANCE = "account";
    String SCOPE_GET_ACCOUNT = "svc-mgmt-agg-cust-id-acc-num";

    String shahkar(String token ,String nationalCode, String mobile) throws ChannelException, URISyntaxException;
    String getToken(String clientId, String clientSecret, String scope) throws ChannelException, URISyntaxException;
    String sendSms(String token, String message, String mobile, int priority, int ttl) throws ChannelException, URISyntaxException;
    String fundTransfer(String token, String traceId, String requestId, String fromAccount, String toAccount, String amount, Date date, String creditPayId) throws ChannelException, URISyntaxException;
    String fundTransferInquiry(String token, String uuid, String amount) throws ChannelException, URISyntaxException;
    String statement(String token, String amount, String srcAccountNumber, String traceNumber, String fromDate, String toDate, String creditDebit, int page, int length) throws ChannelException, URISyntaxException;
    String getAccount(String token, String nationalCode, String customerType) throws ChannelException, URISyntaxException;
    String getBalance(String token, String accountNumber) throws ChannelException, URISyntaxException;
}
