package com.melli.hub.util.pdf.sina.travel;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString
@RequiredArgsConstructor
public class CoverageData {
    @JsonProperty("brokerCoverages")
    private List<Coverage> coverages;
}
