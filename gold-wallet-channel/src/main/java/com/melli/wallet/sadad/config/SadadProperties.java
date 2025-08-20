package com.melli.wallet.sadad.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "sadad")
public class SadadProperties {
    
    private String timeout;
    private String identityUrl;
    private String fundTransferUrl;
    private String inquiryUrl;
    private String shahkarUrl;
    private String statementUrl;
    private String smsUrl;
    private String mobileUrl;
    private String balanceUrl;
    private String accountUrl;
}
