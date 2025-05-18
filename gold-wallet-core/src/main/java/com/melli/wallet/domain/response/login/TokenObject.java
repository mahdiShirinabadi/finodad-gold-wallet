package com.melli.wallet.domain.response.login;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.melli.wallet.NamingProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

/**
 * Class Name: AccessToken
 * Author: Mahdi Shirinabadi
 * Date: 1/8/2025
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class TokenObject {

    @Schema(name = NamingProperty.TOKEN)
    @JsonProperty(NamingProperty.TOKEN)
    private String token;

    @Schema(name = NamingProperty.EXPIRE_TIME)
    @JsonProperty(NamingProperty.EXPIRE_TIME)
    private long expireTime;
}
