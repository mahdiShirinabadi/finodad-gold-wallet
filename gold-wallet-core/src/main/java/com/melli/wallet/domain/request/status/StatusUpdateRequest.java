package com.melli.wallet.domain.request.status;

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
 * Class Name: StatusUpdateRequest
 * Author: AI Assistant
 * Date: 1/26/2025
 * Description: Request DTO for updating status
 */
@Setter
@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class StatusUpdateRequest {

    @Schema(description = "Status ID", example = "1")
    @NotNull(message = "Status ID is required")
    @NumberValidation
    private String id;

    @Schema(description = "Status code", example = "1001")
    @NotNull(message = "Status code is required")
    @StringValidation
    private String code;

    @Schema(description = "Persian description", example = "عملیات موفق")
    @NotNull(message = "Persian description is required")
    @StringValidation
    private String persianDescription;

    @Schema(description = "Additional data", example = "Additional information")
    private String additionalData;
}
