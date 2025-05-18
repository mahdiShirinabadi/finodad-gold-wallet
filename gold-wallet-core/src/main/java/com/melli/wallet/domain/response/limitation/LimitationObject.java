package com.melli.wallet.domain.response.limitation;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.melli.wallet.NamingProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

/**
 * Class Name: LimitationObject
 * Author: Mahdi Shirinabadi
 * Date: 4/23/2025
 */
@Setter
@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class LimitationObject {

    @Schema(name = NamingProperty.NAME)
    @JsonProperty(NamingProperty.NAME)
    private String name;

    @Schema(name = NamingProperty.DESCRIPTION)
    @JsonProperty(NamingProperty.DESCRIPTION)
    private String description;
}
