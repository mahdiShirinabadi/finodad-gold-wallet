package com.melli.wallet.domain.response.base;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.melli.wallet.NamingProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
public class PageResponse {

    @Schema(name = NamingProperty.SIZE)
    @JsonProperty(NamingProperty.SIZE)
    private int size;

    @Schema(name = NamingProperty.NUMBER)
    @JsonProperty(NamingProperty.NUMBER)
    private int number;

    @Schema(name = NamingProperty.TOTAL_PAGES)
    @JsonProperty(NamingProperty.TOTAL_PAGES)
    private long totalPages;

    @Schema(name = NamingProperty.TOTAL_ELEMENTS)
    @JsonProperty(NamingProperty.TOTAL_ELEMENTS)
    private long totalElements;
}
