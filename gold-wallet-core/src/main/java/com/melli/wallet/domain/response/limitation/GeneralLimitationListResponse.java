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
 * Class Name: GeneralLimitationListResponse
 * Author: Mahdi Shirinabadi
 * Date: 1/20/2025
 */
@Setter
@Getter
@ToString
public class GeneralLimitationListResponse extends PageResponse {

    @Schema(name = "generalLimitationList")
    @JsonProperty("generalLimitationList")
    private List<GeneralLimitationObject> generalLimitationList;

    public GeneralLimitationListResponse(List<GeneralLimitationObject> generalLimitationList, int size, int number, long totalPages, long totalElements) {
        super();
        this.generalLimitationList = generalLimitationList;
        this.setSize(size);
        this.setNumber(number);
        this.setTotalPages(totalPages);
        this.setTotalElements(totalElements);
    }

    public GeneralLimitationListResponse() {
        super();
    }
} 