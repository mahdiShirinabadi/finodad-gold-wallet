package com.melli.wallet.domain.request.login;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.melli.wallet.NamingProperty;
import com.melli.wallet.annotation.string.StringValidation;
import io.swagger.v3.oas.annotations.media.Schema;
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
    @Schema(name = NamingProperty.USERNAME)
    @JsonProperty(value = NamingProperty.USERNAME, required = true)
    private String username;

    @NotNull(message = "refreshToken نمی تواند خالی باشد")
    @StringValidation(label = "refreshToken")
    @Schema(name = NamingProperty.REFRESH_TOKEN)
    @JsonProperty(value = NamingProperty.REFRESH_TOKEN, required = true)
    private String refreshToken;
}
