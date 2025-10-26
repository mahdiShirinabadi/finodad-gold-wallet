package com.melli.wallet.domain.request.stat;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request for regenerating statistics")
public class StatRegenerateRequest {

    @NotBlank(message = "Stat type is required")
    @Schema(description = "Type of statistics to regenerate", example = "buy", 
            allowableValues = {"buy", "sell", "wallet", "person2person", "physical-cash-out"})
    private String statType;

    @Schema(description = "Start date in Persian format (yyyy/MM/dd)", example = "1403/01/01")
    private String fromDate;

    @Schema(description = "End date in Persian format (yyyy/MM/dd)", example = "1403/01/31")
    private String toDate;
}
