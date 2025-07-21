package com.melli.wallet.domain.response.limitation;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.melli.wallet.NamingProperty;
import com.melli.wallet.domain.response.base.PageResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

/**
 * Class Name: GeneralCustomLimitationListResponse
 * Author: Mahdi Shirinabadi
 * Date: 1/20/2025
 */
@Setter
@Getter
@ToString
public class GeneralCustomLimitationListResponse extends PageResponse {

    @Schema(name = "generalCustomLimitationList")
    @JsonProperty("generalCustomLimitationList")
    private List<GeneralCustomLimitationObject> generalCustomLimitationList;

    public GeneralCustomLimitationListResponse(List<GeneralCustomLimitationObject> generalCustomLimitationList, int size, int number, long totalPages, long totalElements) {
        super();
        this.generalCustomLimitationList = generalCustomLimitationList;
        this.setSize(size);
        this.setNumber(number);
        this.setTotalPages(totalPages);
        this.setTotalElements(totalElements);
    }

    public GeneralCustomLimitationListResponse() {
        super();
    }
} 