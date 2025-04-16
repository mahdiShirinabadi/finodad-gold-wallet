package com.melli.wallet.ipg.impl;


import com.melli.wallet.ChannelException;
import com.melli.wallet.ipg.SadadPspChannelInterface;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.net.URISyntaxException;


@Profile({"test"})
@Component
@Log4j2
public class TestSadadPspChannelInterfaceImpl implements SadadPspChannelInterface {

    @Override
    public String requestToken(String uuid, String merchantId, String terminalId, long amount, long orderId, String returnUrl, String additionalData, String merchantKey, String mobileNumber, String nationalCode) throws ChannelException, URISyntaxException {
        return "{\"ResCode\":\"0\",\"Token\":\"0001B410BCD7BD860849B4653358DEBB8B5BAFC67359F871E361\",\"Description\":\"عملیات با موفقیت انجام شد\"}";
    }

    @Override
    public String verify(String uuid, String token, String merchantKey) throws ChannelException, URISyntaxException {
        if(token.equalsIgnoreCase("WITHERROR")){
            return "{\n" +
                    "    \"ResCode\": \"100\",\n" +
                    "    \"Description\": \"درخواست قبلا در سیسنم ثبت شده است(درخواست تکراری است)\",\n" +
                    "    \"Amount\": \"50000\",\n" +
                    "    \"RetrivalRefNo\": \"324970354312\",\n" +
                    "    \"SystemTraceNo\": \"002046\",\n" +
                    "    \"OrderId\": \"66\",\n" +
                    "    \"SwitchResCode\": \"00\",\n" +
                    "    \"TransactionDate\": \"11/18/2024 12:41:19 PM\",\n" +
                    "    \"AdditionalData\": \"123\",\n" +
                    "    \"CardHolderFullName\": null\n" +
                    "}";
        }
        return "{\"ResCode\":\"0\",\"Description\":\"عملیات ناموفق بود\",\"Amount\":\"0\",\"RetrivalRefNo\":null,\"SystemTraceNo\":null,\"OrderId\":null,\"SwitchResCode\":null,\"TransactionDate\":\"1/1/0001 12:00:00 AM\",\"AdditionalData\":null,\"CardHolderFullName\":null}";
    }
}
