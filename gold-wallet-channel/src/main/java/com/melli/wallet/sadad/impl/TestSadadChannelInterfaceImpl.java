package com.melli.wallet.sadad.impl;

import com.melli.wallet.ChannelException;
import com.melli.wallet.sadad.SadadChannelInterface;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.net.URISyntaxException;

@Service
@Profile({"test","dev"})
@Log4j2
public class TestSadadChannelInterfaceImpl implements SadadChannelInterface {

    @Value("${sadad.timeout}")
    private String timeout;

    @Value("${sadad.sms.url}")
    private String smsUrl;


    @Override
    public String shahkar(String token, String nationalCode, String mobile) throws ChannelException, URISyntaxException {
        if (mobile.equalsIgnoreCase("09124162337")) {
            return """
                    {"error":null,"response":{"response":200,"result":"OK","comment":"درخواست با موفقیت ثبت شد","inquiryDate":"2024-07-23T08:35:31.331","online":true}}
                    """;
        }
        return """
                {"error":null,"response":{"response":600,"result":"NotIdentifiedException","comment":"اطلاعات ارسالی با اطلاعات موجود در سامانه در یکی یا بیشتر از فیلدهای زیر مشابهت ندارد.(نام،نام خانوادگی،نام پدر،تاریخ تولد،جنسیت برای حقیقی ) (شماره ثبت شرکت،نام شرکت،تاریخ ثبت،فعال یا غیر فعال بودن برای حقوقی ایرانی)","inquiryDate":null,"online":true}}
                """;
    }

    @Override
    public String getToken(String clientId, String clientSecret, String scope) throws ChannelException, URISyntaxException {
        return """
                {"last_logins":"[]","token_type":"bearer","expires_in":86400,"access_token":"eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJncmFudCI6IkNMSUVOVCIsImlzcyI6Imh0dHA6Ly9hcGkuYm1pLmlyL3NlY3VyaXR5IiwiYXVkIjoiZmlub2RhZC1zdWt1ay1jbGllbnQiLCJleHAiOjE3MzM2MjkxMTc2NzQsIm5iZiI6MTczMzU0MjcxNzY3NCwicm9sZSI6IiIsInNlcmlhbCI6ImYzNTI4M2IwLTM1Y2ItM2E2Ni1hODc3LTdjMTFiMjczOTMyZSIsInNzbiI6IjExMTExMTUzOSIsImNsaWVudF9pZCI6IjExMTExMTUzOSIsInNjb3BlcyI6WyJzdmMtbWdtdC1hY2Mtc3RtdC1vbCJdfQ==.zJtHg0M8_33WgmRwuW99VCX9Qc2jjNyuWI2cBqba15Y"}
                """;
    }

    @Override
    public String sendSms(String token, String message, String mobile, int priority, int ttl) throws ChannelException, URISyntaxException {
        log.info("start statementUrl({}), mobile ({}),timeout({})", smsUrl, mobile, timeout);
        return "OK";
    }
}
