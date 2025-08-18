package com.melli.wallet.domain.response.transaction;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.melli.wallet.NamingProperty;
import com.melli.wallet.domain.response.base.PageResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

/**
 * Class Name: StatemnetResponse
 * Author: Mahdi Shirinabadi
 * Date: 8/18/2025
 */
@Setter
@Getter
@ToString
public class StatementResponse {

    @Schema(name = NamingProperty.NATIONAL_CODE)
    @JsonProperty(NamingProperty.NATIONAL_CODE)
    private String nationalCode;

    @Schema(name = NamingProperty.TRANSACTION_OBJECT_LIST)
    @JsonProperty(NamingProperty.TRANSACTION_OBJECT_LIST)
    private List<StatementObject> list;

}
