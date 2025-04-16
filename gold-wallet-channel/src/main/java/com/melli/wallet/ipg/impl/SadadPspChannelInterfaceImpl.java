package com.melli.wallet.ipg.impl;


import com.melli.wallet.ChannelException;
import com.melli.wallet.ipg.SadadPspChannelInterface;
import com.melli.wallet.util.Utility;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Security;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static com.melli.wallet.WebCallUtils.sendRequest;


@Profile({"prod"})
@Component
@Log4j2
public class SadadPspChannelInterfaceImpl implements SadadPspChannelInterface {


    @Value("${sadad.psp.base.path}")
    private String basePath;

    @Value("${sadad.psp.timeout}")
    private int timeout;
    @Value("${sadad.psp.getTokenUrl}")
    private String sadadToken;
    @Value("${sadad.psp.verifyUrl}")
    private String sadadVerify;


    @Override
    public String requestToken(String uuid, String merchantId, String terminalId, long amount,
                               long id, String returnUrl, String additionalData,
                               String merchantKey, String mobileNumber, String nationalCode) throws ChannelException, URISyntaxException {

        Map<String, String> headers = new HashMap<>();
        headers.put("Content-type", "application/json");
        headers.put("charset", "UTF-8");

        Map<String, String> param = new HashMap<>();

        SimpleDateFormat simeDateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        String localTime = simeDateFormat.format(new Date());

        JSONObject jsonRequest = new JSONObject();
        jsonRequest.put("TerminalId", terminalId);
        jsonRequest.put("MerchantId", merchantId);
        jsonRequest.put("Amount", amount);
        jsonRequest.put("OrderId", id);
        jsonRequest.put("ReturnUrl", returnUrl);
        jsonRequest.put("AdditionalData", additionalData);
        jsonRequest.put("LocalDateTime", localTime);
        jsonRequest.put("UserId", Utility.cleanPhoneNumberAddZero(mobileNumber));
        jsonRequest.put("PanAuthenticationType", "2");
        jsonRequest.put("CardHolderIdentity", Utility.cleanPhoneNumberAddZero(mobileNumber));
        jsonRequest.put("NationalCode", nationalCode);

        jsonRequest.put("SignData", generateSign(terminalId, String.valueOf(id), String.valueOf(amount), merchantKey));
        log.info("start call getIpgToken url {}, uuid ({}) with data {}", sadadToken, uuid, jsonRequest);

        StringEntity body = new StringEntity(jsonRequest.toString(), "UTF-8");
        URIBuilder builder = new URIBuilder(basePath).setPath(sadadToken);
        String response = sendRequest(Utility.getCallerClassAndMethodName(), HttpMethod.POST, builder, timeout, headers, param, body, new int[]{HttpStatus.OK.value()});

        log.info("({}), Finished for uuid ({}), with response({})", Utility.getCallerMethodName(), uuid, response);
        return response;
    }

    @Override
    public String verify(String uuid, String token, String merchantKey) throws ChannelException, URISyntaxException {

        Map<String, String> param = new HashMap<>();

        Map<String, String> headers = new HashMap<>();
        headers.put("Content-type", "application/json");
        headers.put("charset", "UTF-8");

        JSONObject jsonRequest = new JSONObject();
        jsonRequest.put("Token", token);
        jsonRequest.put("SignData", generateVerifySign(token, merchantKey));


        StringEntity body = new StringEntity(jsonRequest.toString(), "UTF-8");
        URIBuilder builder = new URIBuilder(basePath).setPath(sadadVerify);

        log.info("start call verify url {}, uuid ({}) with data {}", builder.toString(), uuid, jsonRequest);

        String response = sendRequest(Utility.getCallerClassAndMethodName(), HttpMethod.POST, builder, timeout, headers, param, body, new int[]{HttpStatus.OK.value()});
        log.info("({}), Finished for uuid ({}), with response({})", Utility.getCallerMethodName(), uuid, response);
        return response;

    }

    public String generateSign(String terminalId, String orderId, String amount, String merchantKey) {
        String signInput = terminalId + ";" + orderId + ";" + amount;
        try {
            return new String(Base64.encodeBase64(encode(signInput.getBytes(),
                    Base64.decodeBase64(merchantKey))));
        } catch (Exception ex) {
            return null;
        }
    }

    public String generateVerifySign(String token, String merchantKey) {

        try {
            return new String(Base64.encodeBase64(encode(token.getBytes(),
                    Base64.decodeBase64(merchantKey))));
        } catch (Exception ex) {
            return null;
        }
    }


    public byte[] encode(byte[] input, byte[] key)
            throws IllegalBlockSizeException, BadPaddingException,
            NoSuchAlgorithmException, NoSuchProviderException,
            NoSuchPaddingException, InvalidKeyException {

        Security.addProvider(new BouncyCastleProvider());
        SecretKey keySpec = new SecretKeySpec(key, "DESede");
        Cipher encryptors = Cipher.getInstance("DESede/ECB/PKCS7Padding", "BC");
        encryptors.init(Cipher.ENCRYPT_MODE, keySpec);
        return encryptors.doFinal(input);
    }

}
