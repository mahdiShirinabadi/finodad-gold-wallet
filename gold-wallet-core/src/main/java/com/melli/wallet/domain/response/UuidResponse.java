package com.melli.wallet.domain.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Setter
@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class UuidResponse {

    @JsonProperty("uniqueIdentifier")
    private String uniqueIdentifier;
}
