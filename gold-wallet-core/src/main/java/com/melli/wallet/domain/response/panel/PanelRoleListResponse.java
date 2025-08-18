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
@AllArgsConstructor
@NoArgsConstructor
public class PanelRoleListResponse extends PageResponse {

    @Schema(name = NamingProperty.LIST, description = "لیست نقش ها")
    @JsonProperty(NamingProperty.LIST)
    private List<PanelRoleObject> list;
}