package com.melli.hub.domain.response.login;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.melli.hub.domain.response.channel.ChannelObject;
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

    @JsonProperty("channelObject")
    private ChannelObject channelObject;

    @JsonProperty("accessTokenObject")
    private TokenObject accessTokenObject;

    @JsonProperty("refreshTokenObject")
    private TokenObject refreshTokenObject;
}
