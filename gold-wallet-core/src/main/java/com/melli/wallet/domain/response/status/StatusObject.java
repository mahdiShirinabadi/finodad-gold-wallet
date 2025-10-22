package com.melli.wallet.domain.response.status;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;

/**
 * Class Name: StatusObject
 * Author: AI Assistant
 * Date: 1/26/2025
 * Description: Status object for response
 */
@Setter
@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class StatusObject {

    @Schema(description = "Status ID")
    @JsonProperty("id")
    private Long id;

    @Schema(description = "Status code")
    @JsonProperty("code")
    private String code;

    @Schema(description = "Persian description")
    @JsonProperty("persianDescription")
    private String persianDescription;

    @Schema(description = "Additional data")
    @JsonProperty("additionalData")
    private String additionalData;

    @Schema(description = "Creation date")
    @JsonProperty("createdAt")
    private Date createdAt;

    @Schema(description = "Last update date")
    @JsonProperty("updatedAt")
    private Date updatedAt;
}
