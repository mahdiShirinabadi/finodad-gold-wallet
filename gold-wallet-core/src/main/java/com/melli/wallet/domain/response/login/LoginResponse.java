package com.melli.wallet.domain.response.login;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.melli.wallet.NamingProperty;
import com.melli.wallet.domain.response.channel.ChannelObject;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

/**
 * Created by shirinabadi on 1/23/2017.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class LoginResponse {

    @Schema(name = NamingProperty.CHANNEL_OBJECT)
    @JsonProperty(NamingProperty.CHANNEL_OBJECT)
    private ChannelObject channelObject;

    @Schema(name = NamingProperty.ACCESS_TOKEN_OBJECT)
    @JsonProperty(NamingProperty.ACCESS_TOKEN_OBJECT)
    private TokenObject accessTokenObject;

    @Schema(name = NamingProperty.REFRESH_TOKEN_OBJECT)
    @JsonProperty(NamingProperty.REFRESH_TOKEN_OBJECT)
    private TokenObject refreshTokenObject;
}
