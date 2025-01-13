package com.melli.hub.domain.response.login;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.melli.hub.annotation.national_code.NationalCodeValidation;
import com.melli.hub.annotation.password.StrongPasswordValidation;
import com.melli.hub.annotation.string.StringValidation;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
public class ForgetPasswordUpdatePasswordRequestJson {

    @NationalCodeValidation(label = "کد ملی")
    @JsonProperty(value = "nationalCode",required = true)
    private String nationalCode;

    @JsonProperty("hashData")
    private String hashData;

    @StringValidation(label = "رمز یکبار مصرف")
    @JsonProperty(value = "otp",required = true)
    private String otp;

    @StringValidation(label = "رمز عبور")
    @ToString.Exclude
    @JsonProperty(value = "password", required = true)
    @StrongPasswordValidation(containLowerCase = true, containNOSpace = true, containUpperCase = true,
            containOneDigit = true, containSpecialCharacter = false, label = "رمز عبور")
    private String password;

    @StringValidation(label = "تکرار رمز عبور")
    @ToString.Exclude
    @JsonProperty(value = "repeatPassword", required = true)
    private String repeatPassword;
}
