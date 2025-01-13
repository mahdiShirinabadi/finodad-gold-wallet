package com.melli.hub.sadad.impl;

import com.melli.hub.ChannelException;
import com.melli.hub.sadad.SadadChannelInterface;
import com.melli.hub.util.Utility;
import lombok.extern.log4j.Log4j2;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
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

import static com.melli.hub.WebCallUtils.sendRequest;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@Service
@Profile({"staging"})
@Log4j2
public class SimpleSadadChannelInterfaceImpl implements SadadChannelInterface {

    @Value("${sadad.timeout}")
    private String timeout;
    @Value("${sadad.identity.url}")
    private String identityUrl;
    @Value("${sadad.shahkar.url}")
    private String shahkarUrl;
    @Value("${sadad.sms.url}")
    private String smsUrl;


    @Override
    public String shahkar(String token, String nationalCode, String mobile) throws ChannelException, URISyntaxException {
        log.info("start shahkarUrl({}), nationalCode ({}), timeout({})", shahkarUrl, nationalCode, timeout);

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
        json.put("acceptDays", "20");
        json.put("branchCode", "0");
        StringEntity body = new StringEntity(json.toString(), StandardCharsets.UTF_8);

        URIBuilder builder = new URIBuilder(shahkarUrl);

        String response = sendRequest(Utility.getCallerClassAndMethodName(), HttpMethod.POST, builder, Integer.parseInt(timeout), headers, params, body, new int[] {HttpStatus.OK.value()});
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
        URIBuilder builder = new URIBuilder(identityUrl);
        String response = sendRequest(Utility.getCallerClassAndMethodName(), HttpMethod.POST, builder, Integer.parseInt(timeout), headers, params, null, new int[] {HttpStatus.OK.value()});
        log.info("({}), Finished login  for scope ({}) with response({})", Utility.getCallerMethodName(), scope, response);
        return response;
    }

    @Override
    public String sendSms(String token, String message, String mobile, int priority, int ttl) throws ChannelException, URISyntaxException {
       /* log.info("({}) Start, mobile ({}),timeout({})", Utility.getCallerMethodName(), mobile, timeout);

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

        URIBuilder builder = new URIBuilder(smsUrl);

        log.info("send message with jsonRequest ({})", json.toString());

        String response = sendRequest(Utility.getCallerClassAndMethodName(), HttpMethod.POST, builder, Integer.parseInt(timeout), headers, params, body, new int[] {HttpStatus.OK.value()});
        log.info("({}), Finished for, with response({})", Utility.getCallerMethodName(), response);
        return response;*/
        return "ok";
    }
}
