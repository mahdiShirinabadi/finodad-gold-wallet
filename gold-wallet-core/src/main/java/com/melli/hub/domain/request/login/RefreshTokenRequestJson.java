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
    @StringValidation(label = "نام کاربری")
    @JsonProperty(value = "username", required = true)
    private String username;

    @NotNull(message = "refreshToken نمی تواند خالی باشد")
    @StringValidation(label = "refreshToken")
    @JsonProperty(value = "refreshToken", required = true)
    private String refreshToken;
}
