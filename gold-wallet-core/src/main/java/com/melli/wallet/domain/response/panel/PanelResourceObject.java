package com.melli.wallet.domain.response.panel;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.melli.wallet.NamingProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Setter
@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class PanelResourceObject {

    @Schema(name = NamingProperty.ID, description = "شناسه منبع")
    @JsonProperty(NamingProperty.ID)
    private String id;
    
    @Schema(name = NamingProperty.NAME, description = "نام منبع")
    @JsonProperty(NamingProperty.NAME)
    private String name;

    @Schema(name = NamingProperty.CREATE_TIME, description = "زمان ایجاد")
    @JsonProperty(NamingProperty.CREATE_TIME)
    private String createdAt;
    
    @Schema(name = NamingProperty.CREATE_BY, description = "ایجاد کننده")
    @JsonProperty(NamingProperty.CREATE_BY)
    private String createdBy;
    
    @Schema(name = NamingProperty.UPDATE_TIME, description = "زمان بروزرسانی")
    @JsonProperty(NamingProperty.UPDATE_TIME)
    private String updatedAt;
    
    @Schema(name = NamingProperty.UPDATE_BY, description = "بروزرسانی کننده")
    @JsonProperty(NamingProperty.UPDATE_BY)
    private String updatedBy;
}