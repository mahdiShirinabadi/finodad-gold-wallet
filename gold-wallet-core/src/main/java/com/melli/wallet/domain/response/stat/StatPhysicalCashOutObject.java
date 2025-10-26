package com.melli.wallet.domain.response.stat;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Class Name: StatPhysicalCashOutObject
 * Author: AI Assistant
 * Date: 1/26/2025
 * Description: DTO for physical cash out statistics object
 */
@Setter
@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class StatPhysicalCashOutObject {

    @Schema(description = "Statistics ID")
    private Long id;

    @Schema(description = "Channel ID")
    private Long channelId;

    @Schema(description = "Currency ID")
    private Long currencyId;

    @Schema(description = "Result code")
    private String result;

    @Schema(description = "Count of transactions")
    private Long count;

    @Schema(description = "Total amount")
    private BigDecimal amount;

    @Schema(description = "Persian calculation date")
    private String persianCalcDate;

    @Schema(description = "Georgian calculation date")
    private Date georgianCalcDate;

    @Schema(description = "Created at")
    private Date createdAt;

    @Schema(description = "Updated at")
    private Date updatedAt;
}
