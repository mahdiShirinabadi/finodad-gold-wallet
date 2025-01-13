package com.melli.hub.domain.request.login;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.melli.hub.annotation.mobile.MobileValidation;
import com.melli.hub.annotation.national_code.NationalCodeValidation;
import com.melli.hub.annotation.password.StrongPasswordValidation;
import com.melli.hub.annotation.string.StringValidation;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
public class SignUpPasswordRequestJson {

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

    @StrongPasswordValidation(label = "رمز عبور", containOneDigit = true, containLowerCase = true, containUpperCase = true, containNOSpace = true)
    @StringValidation(label = "رمز عبور")
    @JsonProperty(value = "password", required = true)
    private String password;

    @StringValidation(label = "تکرار رمز عبور")
    @JsonProperty(value = "repeatPassword", required = true)
    private String repeatPassword;

}
