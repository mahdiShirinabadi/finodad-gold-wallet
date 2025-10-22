package com.melli.wallet.domain.request.template;

import com.melli.wallet.annotation.number.NumberValidation;
import com.melli.wallet.annotation.string.StringValidation;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Class Name: TemplateUpdateRequest
 * Author: AI Assistant
 * Date: 1/26/2025
 * Description: Request DTO for updating template
 */
@Setter
@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class TemplateUpdateRequest {

    @Schema(description = "Template ID", example = "1")
    @NotNull(message = "Template ID is required")
    @NumberValidation
    private String id;

    @Schema(description = "Template name", example = "p2p_deposit")
    @NotNull(message = "Template name is required")
    @StringValidation
    private String name;

    @Schema(description = "Template value", example = "P2P Deposit Template")
    @NotNull(message = "Template value is required")
    @StringValidation
    private String value;
}
