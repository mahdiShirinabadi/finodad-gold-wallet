package com.melli.wallet.domain.response.panel;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.melli.wallet.NamingProperty;
import com.melli.wallet.domain.response.base.PageResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.List;

@Setter
@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class PanelResourceListResponse extends PageResponse {

    @Schema(name = NamingProperty.LIST, description = "لیست منابع")
    @JsonProperty(NamingProperty.LIST)
    private List<PanelResourceObject> list;
}