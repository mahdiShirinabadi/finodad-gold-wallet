package com.melli.wallet.domain.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.melli.wallet.NamingProperty;
import com.melli.wallet.domain.response.base.PageResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.List;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class PanelChannelResponse extends PageResponse {

    @Schema(name = NamingProperty.LIST, description = "لیست کانال ها")
    @JsonProperty(NamingProperty.LIST)
    private List<PanelChannelObject> list;

}
