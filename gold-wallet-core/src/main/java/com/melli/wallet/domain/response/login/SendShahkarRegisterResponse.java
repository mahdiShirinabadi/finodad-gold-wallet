package com.melli.wallet.domain.response.login;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

/**
 * Class Name: SendOtpResponse
 * Author: Mahdi Shirinabadi
 * Date: 1/4/2025
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class SendShahkarRegisterResponse {

    @JsonProperty("nationalCode")
    private String nationalCode;

    @JsonProperty("tempUuid")
    private String tempUuid;
}
