package com.melli.wallet.domain.response.limitation;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.melli.wallet.NamingProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

/**
 * Class Name: LimitationResponse
 * Author: Mahdi Shirinabadi
 * Date: 4/23/2025
 */
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class LimitationCustomResponse {

    @Schema(name = NamingProperty.VALUE)
    @JsonProperty(NamingProperty.VALUE)
    private String value;
}
