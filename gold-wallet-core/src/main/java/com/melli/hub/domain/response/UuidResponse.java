package com.melli.hub.domain.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
@AllArgsConstructor
public class UuidResponse {

    @JsonProperty("uniqueIdentifier")
    private String uniqueIdentifier;
}
