package com.melli.wallet.domain.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Setter
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class PanelBaseSearchJson {

    private static final String TEXT_BLOCK = """
            {
                "key":"value"
            }
            """;

    @JsonProperty("parameterMap")
    @Schema(name = "parameterMap", title = "parameterMap", description = "", example = TEXT_BLOCK)
    private Map<String, String> map;

    @Override
    public String toString() {
        return "PanelSearchJson{" +
                "map=" + map +
                '}';
    }
}
