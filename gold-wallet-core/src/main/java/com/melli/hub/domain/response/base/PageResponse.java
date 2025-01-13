package com.melli.hub.domain.response.base;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
public class PageResponse {

    @JsonProperty("size")
    private int size;

    @JsonProperty("number")
    private int number;

    @JsonProperty("totalPages")
    private long totalPages;

    @JsonProperty("totalElements")
    private long totalElements;
}
