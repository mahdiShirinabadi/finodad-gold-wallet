package com.melli.wallet.domain.response.limitation;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.melli.wallet.NamingProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.List;

/**
 * Class Name: LimitationListResponse
 * Author: Mahdi Shirinabadi
 * Date: 4/23/2025
 */
@Setter
@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class LimitationListResponse {

    @Schema(name = NamingProperty.LIMITATION_OBJECT_LIST)
    @JsonProperty(NamingProperty.LIMITATION_OBJECT_LIST)
    private List<LimitationObject> limitationObjectList;
}
