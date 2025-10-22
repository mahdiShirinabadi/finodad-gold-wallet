package com.melli.wallet.domain.response.template;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;

/**
 * Class Name: TemplateObject
 * Author: AI Assistant
 * Date: 1/26/2025
 * Description: Template object for response
 */
@Setter
@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class TemplateObject {

    @Schema(description = "Template ID")
    @JsonProperty("id")
    private Long id;

    @Schema(description = "Template name")
    @JsonProperty("name")
    private String name;

    @Schema(description = "Template value")
    @JsonProperty("value")
    private String value;

    @Schema(description = "Creation date")
    @JsonProperty("createdAt")
    private Date createdAt;

    @Schema(description = "Last update date")
    @JsonProperty("updatedAt")
    private Date updatedAt;
}
