package com.melli.hub.domain.request.login;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.melli.hub.annotation.mobile.MobileValidation;
import com.melli.hub.annotation.national_code.NationalCodeValidation;
import com.melli.hub.annotation.number.NumberValidation;
import com.melli.hub.annotation.string.StringValidation;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
public class SignUpShahkarRequestJson {

    @NotNull(message = "نام کاربری نمی تواند خالی باشد")
    @NationalCodeValidation(label = "نام کاربری")
    @StringValidation(label = "نام کاربری")
    @JsonProperty(value = "nationalCode", required = true)
    private String nationalCode;

    @MobileValidation
    @StringValidation(label = "شماره همراه")
    @JsonProperty(value = "mobileNumber", required = true)
    private String mobileNumber;

    @StringValidation(label = "کد tempUuid موقت")
    @JsonProperty(value = "tempUuid", required = true)
    private String tempUuid;

    @NumberValidation
    @StringValidation(label = "کد یکبار مصرف")
    @JsonProperty(value = "otp", required = true)
    private String otp;
}
