package com.melli.wallet.domain.response.panel;

import com.melli.wallet.domain.response.base.BaseResponse;
import com.melli.wallet.domain.response.base.PageResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResourceListResponse extends PageResponse {

    private Long id;
    private String name;
    private String faName;
    private Integer display;
}
