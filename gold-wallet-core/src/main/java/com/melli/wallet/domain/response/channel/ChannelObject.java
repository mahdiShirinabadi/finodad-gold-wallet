package com.melli.wallet.domain.response.channel;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.melli.wallet.NamingProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Class Name: ProfileObject
 * Author: Mahdi Shirinabadi
 * Date: 1/4/2025
 */
@Setter
@Getter
@ToString
public class ChannelObject {
    @Schema(name = NamingProperty.FIRST_NAME)
    @JsonProperty(NamingProperty.FIRST_NAME)
    private String firstName;

    @Schema(name = NamingProperty.LAST_NAME)
    @JsonProperty(NamingProperty.LAST_NAME)
    private String lastName;

    @Schema(name = NamingProperty.USERNAME)
    @JsonProperty(NamingProperty.USERNAME)
    private String username;

    @Schema(name = NamingProperty.MOBILE_NUMBER)
    @JsonProperty(NamingProperty.MOBILE_NUMBER)
    private String mobile;
}
