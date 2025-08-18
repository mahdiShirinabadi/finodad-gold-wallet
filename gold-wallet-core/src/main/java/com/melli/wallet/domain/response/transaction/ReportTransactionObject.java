package com.melli.wallet.domain.response.transaction;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.melli.wallet.NamingProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Class Name: ReportStatementOBject
 * Author: Mahdi Shirinabadi
 * Date: 8/18/2025
 */
@Setter
@Getter
@ToString
public class ReportTransactionObject extends StatementObject{

    @Schema(name = NamingProperty.NATIONAL_CODE)
    @JsonProperty(NamingProperty.NATIONAL_CODE)
    private String nationalCode;

}
