package com.melli.wallet.domain.response.login;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class ForgetPasswordCheckOtpResponse {

    @JsonProperty("registerHash")
    private String registerHash;

}
