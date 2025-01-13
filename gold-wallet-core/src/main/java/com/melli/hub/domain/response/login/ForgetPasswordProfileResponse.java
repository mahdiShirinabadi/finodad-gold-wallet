package com.melli.hub.domain.response.login;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class ForgetPasswordProfileResponse {

    @JsonProperty("nationalCode")
    private String nationalCode;

    @JsonProperty("maskMobileNumber")
    private String maskMobileNumber;

    @JsonProperty("otpExpireTime")
    private long otpExpireTime;

    @JsonProperty("registerHash")
    private String registerHash;
}
