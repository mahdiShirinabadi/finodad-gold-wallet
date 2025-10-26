package com.melli.wallet.domain.response.stat;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;

/**
 * Class Name: StatWalletObject
 * Author: AI Assistant
 * Date: 1/26/2025
 * Description: DTO for wallet statistics object
 */
@Setter
@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class StatWalletObject {

    @Schema(description = "Statistics ID")
    private Long id;

    @Schema(description = "Channel ID")
    private Long channelId;

    @Schema(description = "Count of wallets created")
    private Long count;

    @Schema(description = "Persian calculation date")
    private String persianCalcDate;

    @Schema(description = "Georgian calculation date")
    private Date georgianCalcDate;

    @Schema(description = "Created at")
    private Date createdAt;

    @Schema(description = "Updated at")
    private Date updatedAt;
}
