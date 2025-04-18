package com.melli.wallet.domain.request.login;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.melli.wallet.annotation.mobile.MobileValidation;
import com.melli.wallet.annotation.national_code.NationalCodeValidation;
import com.melli.wallet.annotation.string.StringValidation;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
public class ForgetPasswordRequestJson {

    @NotNull(message = "نام کاربری نمی تواند خالی باشد")
    @NationalCodeValidation(label = "نام کاربری")
    @StringValidation(label = "نام کاربری")
    @JsonProperty(value = "nationalCode", required = true)
    private String nationalCode;

    @MobileValidation(label = "شماره همراه")
    @StringValidation(label = "شماره همراه")
    @JsonProperty(value = "mobileNumber", required = true)
    private String mobileNumber;
}
