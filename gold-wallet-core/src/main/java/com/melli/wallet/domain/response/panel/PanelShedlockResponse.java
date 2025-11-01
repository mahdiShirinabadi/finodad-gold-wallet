package com.melli.wallet.domain.response.panel;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.melli.wallet.domain.response.base.PageResponse;
import lombok.*;

import java.util.List;

@Setter
@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class PanelShedlockResponse extends PageResponse {
    @JsonProperty("list")
    List<PanelShedlockObject> list;
}
