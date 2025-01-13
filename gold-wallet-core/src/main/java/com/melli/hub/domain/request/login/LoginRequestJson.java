package com.melli.hub.domain.request.login;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.melli.hub.annotation.string.StringValidation;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
public class LoginRequestJson {

    @NotNull(message = "نام کاربری نمی تواند خالی باشد")
    @StringValidation(label = "نام کاربری")
    @JsonProperty(value = "username", required = true)
    private String username;

    @NotNull(message = "گذرواژه نمی تواند خالی باشد")
    @StringValidation(label = "گذرواژه")
    @JsonProperty(value = "password", required = true)
    @ToString.Exclude
    private String password;

    @StringValidation(label = "اطلاعات دستگاه")
    @JsonProperty(value = "deviceName", required = true)
    private String deviceName;

    @StringValidation(label = "اطلاعات تکمیلی")
    @JsonProperty(value = "additionalData", required = true)
    private String additionalData;

}
