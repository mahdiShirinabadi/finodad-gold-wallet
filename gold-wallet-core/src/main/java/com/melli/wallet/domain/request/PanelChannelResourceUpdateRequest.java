package com.melli.wallet.domain.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.melli.wallet.NamingProperty;
import com.melli.wallet.annotation.number.ListElementsInteger;
import com.melli.wallet.annotation.number.NumberValidation;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Setter
@Getter
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class PanelChannelResourceUpdateRequest {

    @NotNull(message = "شناسه کانال نمی تواند خالی باشد")
    @NumberValidation(label = NamingProperty.ID)
    @JsonProperty(NamingProperty.ID)
    @Schema(name = NamingProperty.ID, description = "شناسه کانال")
    private String id;

    @Schema(name = NamingProperty.ROLE_IDS)
    @JsonProperty(value = NamingProperty.ROLE_IDS)
    private List<String> roleIds;
}
