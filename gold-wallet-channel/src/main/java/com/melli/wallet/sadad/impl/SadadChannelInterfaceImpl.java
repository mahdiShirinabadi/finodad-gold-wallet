package com.melli.wallet.sadad.impl;

import com.melli.wallet.ChannelException;
import com.melli.wallet.sadad.SadadChannelInterface;
import com.melli.wallet.sadad.config.SadadProperties;
import com.melli.wallet.util.CustomStringUtils;
import com.melli.wallet.util.Utility;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.json.JSONObject;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static com.melli.wallet.WebCallUtils.sendRequest;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;


@Service
@Profile({"prod"})
@Log4j2
@RequiredArgsConstructor
public class SadadChannelInterfaceImpl implements SadadChannelInterface {

    private final SadadProperties sadadProperties;


    @Override
    public String fundTransfer(String token, String traceId, String requestId, String fromAccount, String toAccount, String amount, Date date, String creditPayId) throws ChannelException, URISyntaxException {
        log.info("start fundTransfer for url({}), uuid({})", sadadProperties.getFundTransferUrl(), traceId);

        Map<String, String> headers = new HashMap<>();
        headers.put(AUTHORIZATION, "Bearer " + token);
        headers.put("Content-Type", "application/json");

        Map<String, String> params = new HashMap<>();
        params.put("submit", "true");
        params.put("channel", "ISC_MB");

        JSONObject json = new JSONObject();
        json.put("id", traceId);
        json.put("alias", "money-transfer");
        json.put("accountId", fromAccount);
        json.put("instructedAmount", amount);
        json.put("instructedCurrency", "IRR");
        json.put("counterpartyAccount", toAccount);
        json.put("creditPayId", creditPayId);
        json.put("onDate", date.getTime());
        json.put("paymentReference", requestId);
        json.put("paymentDescription", requestId);
        json.put("transferMethod", "INTRA_BANK");
        StringEntity body = new StringEntity(json.toString(), StandardCharsets.UTF_8);

        URIBuilder builder = new URIBuilder(sadadProperties.getFundTransferUrl());
        String response = sendRequest(Utility.getCallerClassAndMethodName(), HttpMethod.POST, builder, Integer.parseInt(sadadProperties.getTimeout()), headers, params, body, new int[] {HttpStatus.OK.value()});
        log.info("({}), Finished for uuid ({}), with response({})", Utility.getCallerMethodName(), traceId, response);
        return response;

    }

    @Override
    public String fundTransferInquiry(String token, String uuid, String amount) throws ChannelException, URISyntaxException {
        log.info("start inquiry for baseUrl({}), timeout({}), uuid ({})", sadadProperties.getInquiryUrl(), sadadProperties.getTimeout(), uuid);

        Map<String, String> headers = new HashMap<>();
        headers.put(AUTHORIZATION, "Bearer " + token);

        Map<String, String> params = new HashMap<>();

        URIBuilder builder = new URIBuilder(sadadProperties.getInquiryUrl() + uuid);
        String response = sendRequest(Utility.getCallerClassAndMethodName(), HttpMethod.GET, builder, Integer.parseInt(sadadProperties.getTimeout()), headers, params, null, new int[] {HttpStatus.OK.value()});
        log.info("({}), Finished for uuid ({}), with response({})", Utility.getCallerMethodName(), uuid, response);
        return response;

    }

    @Override
    public String shahkar(String token, String nationalCode, String mobile) throws ChannelException, URISyntaxException {
        log.info("start shahkarUrl({}), nationalCode ({}), timeout({})", sadadProperties.getShahkarUrl(), nationalCode, sadadProperties.getTimeout());

        Map<String, String> headers = new HashMap<>();
        headers.put(AUTHORIZATION, "Bearer " + token);
        headers.put("Content-Type", "application/json");
        Map<String, String> params = new HashMap<>();

        JSONObject json = new JSONObject();
        json.put("contactValue", mobile);
        json.put("serviceType", "2");
        json.put("identificationType", "0");
        json.put("identificationNo", nationalCode);
        json.put("callerUnitCode", "111111505");
        json.put("online", "TRUE");
        json.put("userIdentificationNo", nationalCode);
        json.put("acceptDays", "10");
        json.put("branchCode", "0");
        StringEntity body = new StringEntity(json.toString(), StandardCharsets.UTF_8);

        URIBuilder builder = new URIBuilder(sadadProperties.getShahkarUrl());

        String response = sendRequest(Utility.getCallerClassAndMethodName(), HttpMethod.POST, builder, Integer.parseInt(sadadProperties.getTimeout()), headers, params, body, new int[] {HttpStatus.OK.value()});
        log.info("({}), Finished for, with response({})", Utility.getCallerMethodName(), response);
        return response;

    }

    @Override
    public String statement(String token, String amount, String srcAccountNumber, String traceNumber, String fromDate, String toDate, String creditDebit, int page, int length) throws ChannelException, URISyntaxException {
        log.info("start statementUrl({}), srcAccountNumber ({}),timeout({}), traceNumber ({})", sadadProperties.getStatementUrl(), srcAccountNumber, sadadProperties.getTimeout(), traceNumber);

        Map<String, String> headers = new HashMap<>();
        headers.put(AUTHORIZATION, "Bearer " + token);
        headers.put("Content-Type", "application/json");
        Map<String, String> params = new HashMap<>();

        JSONObject jsonObjectDate = new JSONObject();
        jsonObjectDate.put("fromDateTime", fromDate);
        jsonObjectDate.put("toDateTime", toDate);
        JSONObject jsonObjectAmount = new JSONObject();
        jsonObjectAmount.put("fromAmount", Long.parseLong(amount));
        jsonObjectAmount.put("toAmount", Long.parseLong(amount));

        JSONObject json = new JSONObject();
        json.put("offsetNumber", page);
        json.put("offsetLength", length);



        json.put("accountNumber", srcAccountNumber);
        json.put("dateTimeRange",jsonObjectDate);
        json.put("amountRange", jsonObjectAmount);
        json.put("creditDebit", creditDebit);
        if(CustomStringUtils.hasText(traceNumber)) {
            json.put("traceNumber", traceNumber);
        }
        StringEntity body = new StringEntity(json.toString(), StandardCharsets.UTF_8);

        URIBuilder builder = new URIBuilder(sadadProperties.getStatementUrl());

        log.info("start get statement with jsonRequest ({})", json.toString());

        String response = sendRequest(Utility.getCallerClassAndMethodName(), HttpMethod.POST, builder, Integer.parseInt(sadadProperties.getTimeout()), headers, params, body, new int[] {HttpStatus.OK.value()});
        log.info("({}), Finished for, with response({})", Utility.getCallerMethodName(), response);
        return response;
    }

    @Override
    public String sendSms(String token, String message, String mobile, int priority, int ttl) throws ChannelException, URISyntaxException {
        log.info("start sendSms({}), mobile ({}),timeout({})", sadadProperties.getSmsUrl(), mobile, sadadProperties.getTimeout());

        Map<String, String> headers = new HashMap<>();
        headers.put(AUTHORIZATION, "Bearer " + token);
        headers.put("Content-Type", "application/json");
        Map<String, String> params = new HashMap<>();

        JSONObject json = new JSONObject();
        json.put("messageText", message);
        json.put("mobileNumber",mobile);
        json.put("priorityNumber", 5);
        json.put("ttl", 3);
        StringEntity body = new StringEntity(json.toString(), StandardCharsets.UTF_8);

        URIBuilder builder = new URIBuilder(sadadProperties.getSmsUrl());

        log.info("send message with jsonRequest ({})", json.toString());

        String response = sendRequest(Utility.getCallerClassAndMethodName(), HttpMethod.POST, builder, Integer.parseInt(sadadProperties.getTimeout()), headers, params, body, new int[] {HttpStatus.OK.value()});
        log.info("({}), Finished for, with response({})", Utility.getCallerMethodName(), response);
        return response;
    }

    @Override
    public String getBalance(String token, String accountNumber) throws ChannelException, URISyntaxException {
        log.info("start inquiry for baseUrl({}), timeout({}), accountNumber ({})", sadadProperties.getBalanceUrl(), sadadProperties.getTimeout(), accountNumber);

        Map<String, String> headers = new HashMap<>();
        headers.put(AUTHORIZATION, "Bearer " + token);

        Map<String, String> params = new HashMap<>();

        URIBuilder builder = new URIBuilder(sadadProperties.getBalanceUrl() + accountNumber);
        String response = sendRequest(Utility.getCallerClassAndMethodName(), HttpMethod.GET, builder, Integer.parseInt(sadadProperties.getTimeout()), headers, params, null, new int[] {HttpStatus.OK.value()});
        log.info("({}), Finished for uuid ({}), with response({})", Utility.getCallerMethodName(), sadadProperties.getBalanceUrl(), response);
        return response;
    }

    @Override
    public String getAccount(String token, String nationalCode, String customerType) throws ChannelException, URISyntaxException {
        log.info("start getAccount({}), url ({}),timeout({})", sadadProperties.getAccountUrl(), nationalCode, sadadProperties.getTimeout());

        Map<String, String> headers = new HashMap<>();
        headers.put(AUTHORIZATION, "Bearer " + token);
        headers.put("Content-Type", "application/json");
        Map<String, String> params = new HashMap<>();

        JSONObject json = new JSONObject();
        json.put("customerType", customerType);
        json.put("identifier", nationalCode);
        StringEntity body = new StringEntity(json.toString(), StandardCharsets.UTF_8);

        URIBuilder builder = new URIBuilder(sadadProperties.getAccountUrl());

        log.info("send message with jsonRequest ({})", json.toString());

        String response = sendRequest(Utility.getCallerClassAndMethodName(), HttpMethod.POST, builder, Integer.parseInt(sadadProperties.getTimeout()), headers, params, body, new int[] {HttpStatus.OK.value()});
        log.info("({}), Finished for, with response({})", Utility.getCallerMethodName(), response);
        return response;
    }

    @Override
    public String getToken(String clientId, String clientSecret, String scope) throws ChannelException, URISyntaxException {

        String valueToEncode = clientId + ":" + clientSecret;
        String authentication = "Basic " + Base64.getEncoder().encodeToString(valueToEncode.getBytes());

        Map<String, String> headers = new HashMap<>();
        headers.put("Content-type", "application/x-www-form-urlencoded");
        headers.put("Authorization", authentication);

        Map<String, String> params = new HashMap<>();
        params.put("scope", scope);
        params.put("grant_type", "client_credentials");
        URIBuilder builder = new URIBuilder(sadadProperties.getIdentityUrl());
        String response = sendRequest(Utility.getCallerClassAndMethodName(), HttpMethod.POST, builder, Integer.parseInt(sadadProperties.getTimeout()), headers, params, null, new int[] {HttpStatus.OK.value()});
        log.info("({}), Finished login  for scope ({}) with response({})", Utility.getCallerMethodName(), scope, response);
        return response;
    }






}
