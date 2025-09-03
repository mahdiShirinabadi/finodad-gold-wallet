package com.melli.wallet.domain.response.p2p;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.melli.wallet.NamingProperty;
import com.melli.wallet.domain.response.UuidResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

/**
 * Class Name: P2pUuidResponse
 * Author: Mahdi Shirinabadi
 * Date: 9/3/2025
 */
@Setter
@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class P2pUuidResponse extends UuidResponse {

    @Schema(name = NamingProperty.DEST_NATIONAL_CODE)
    @JsonProperty(NamingProperty.DEST_NATIONAL_CODE)
    private String destNationalCode;
}
