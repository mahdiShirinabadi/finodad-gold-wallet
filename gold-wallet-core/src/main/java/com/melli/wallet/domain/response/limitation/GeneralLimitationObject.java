package com.melli.wallet.domain.response.limitation;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.melli.wallet.NamingProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Class Name: GeneralLimitationObject
 * Author: Mahdi Shirinabadi
 * Date: 1/20/2025
 */
@Setter
@Getter
@ToString
public class GeneralLimitationObject {

    @Schema(name = NamingProperty.ID)
    @JsonProperty(NamingProperty.ID)
    private String id;

    @Schema(name = NamingProperty.NAME)
    @JsonProperty(NamingProperty.NAME)
    private String name;

    @Schema(name = NamingProperty.VALUE)
    @JsonProperty(NamingProperty.VALUE)
    private String value;

    @Schema(name = NamingProperty.PATTERN)
    @JsonProperty(NamingProperty.PATTERN)
    private String pattern;

    @Schema(name = NamingProperty.ADDITIONAL_DATA)
    @JsonProperty(NamingProperty.ADDITIONAL_DATA)
    private String additionalData;

    @Schema(name = NamingProperty.CREATE_TIME)
    @JsonProperty(NamingProperty.CREATE_TIME)
    private String createTime;

    @Schema(name = NamingProperty.CREATE_BY)
    @JsonProperty(NamingProperty.CREATE_BY)
    private String createBy;

    @Schema(name = NamingProperty.UPDATE_TIME)
    @JsonProperty(NamingProperty.UPDATE_TIME)
    private String updateTime;

    @Schema(name = NamingProperty.UPDATE_BY)
    @JsonProperty(NamingProperty.UPDATE_BY)
    private String updateBy;
} 