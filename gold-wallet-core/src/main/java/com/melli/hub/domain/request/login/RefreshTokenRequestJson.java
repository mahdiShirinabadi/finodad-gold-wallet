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
public class RefreshTokenRequestJson {

    @NotNull(message = "نام کاربری نمی تواند خالی باشد")
    @NationalCodeValidation(label = "نام کاربری")
    @StringValidation(label = "نام کاربری")
    @JsonProperty(value = "nationalCode", required = true)
    private String nationalCode;

    @NotNull(message = "refreshToken نمی تواند خالی باشد")
    @StringValidation(label = "refreshToken")
    @JsonProperty(value = "refreshToken", required = true)
    private String refreshToken;

    @StringValidation(label = "اطلاعات دستگاه")
    @JsonProperty(value = "deviceName", required = true)
    private String deviceName;

    @StringValidation(label = "اطلاعات تکمیلی")
    @JsonProperty(value = "additionalData", required = true)
    private String additionalData;
}
