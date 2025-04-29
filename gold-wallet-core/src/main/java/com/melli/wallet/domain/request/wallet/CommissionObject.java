package com.melli.wallet.domain.request.wallet;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.melli.wallet.annotation.number.NumberValidation;
import com.melli.wallet.annotation.string.StringValidation;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

/**
 * Class Name: CommissionObject
 * Author: Mahdi Shirinabadi
 * Date: 4/26/2025
 */
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class CommissionObject {

    @StringValidation(label = "نوع ارز")
    @Schema(name = "currency", description = "نوع کارمزد GOLD,RIAL, SILVER, PLATINUM", example = "RIAL")
    @JsonProperty("currency")
    private String currency;

    @NumberValidation
    @Schema(name = "amount", description = "مقدار کارمزد", example = "1000")
    @JsonProperty("amount")
    private String amount;
}
