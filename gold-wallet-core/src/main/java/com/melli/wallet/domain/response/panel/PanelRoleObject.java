package com.melli.wallet.domain.response.panel;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.melli.wallet.NamingProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.List;

@Setter
@Getter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PanelRoleObject {

    @Schema(name = NamingProperty.ID, description = "شناسه نقش")
    @JsonProperty(NamingProperty.ID)
    private String id;
    
    @Schema(name = NamingProperty.NAME, description = "نام نقش")
    @JsonProperty(NamingProperty.NAME)
    private String name;
    
    @Schema(name = NamingProperty.ADDITIONAL_DATA, description = "داده های اضافی")
    @JsonProperty(NamingProperty.ADDITIONAL_DATA)
    private String additionalData;
    
    @Schema(name = NamingProperty.CREATE_TIME, description = "زمان ایجاد")
    @JsonProperty(NamingProperty.CREATE_TIME)
    private String createdTime;
    
    @Schema(name = NamingProperty.CREATE_BY, description = "ایجاد کننده")
    @JsonProperty(NamingProperty.CREATE_BY)
    private String createdBy;
    
    @Schema(name = NamingProperty.UPDATE_TIME, description = "زمان بروزرسانی")
    @JsonProperty(NamingProperty.UPDATE_TIME)
    private String updatedTime;
    
    @Schema(name = NamingProperty.UPDATE_BY, description = "بروزرسانی کننده")
    @JsonProperty(NamingProperty.UPDATE_BY)
    private String updatedBy;
    
    @Schema(name = NamingProperty.RESOURCES, description = "منابع نقش")
    @JsonProperty(NamingProperty.RESOURCES)
    private List<PanelResourceObject> resources;
}