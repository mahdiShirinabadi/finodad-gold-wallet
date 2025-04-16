package com.melli.wallet.domain.request.login;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.melli.wallet.annotation.national_code.NationalCodeValidation;
import com.melli.wallet.annotation.string.StringValidation;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
public class ForgetPasswordOtpRequestJson {

    @NationalCodeValidation(label = "کد ملی")
    @JsonProperty(value = "nationalCode",required = true)
    private String nationalCode;

    @StringValidation(label = "عبارت رمز شده")
    @JsonProperty(value = "hashData",required = true)
    private String hashData;

    @StringValidation(label = "رمز یکبار مصرف")
    @JsonProperty(value = "otp",required = true)
    private String otp;
}
