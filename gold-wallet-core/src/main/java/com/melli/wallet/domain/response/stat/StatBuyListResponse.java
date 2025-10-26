package com.melli.wallet.domain.response.stat;

import com.melli.wallet.domain.response.base.PageResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

/**
 * Class Name: StatBuyListResponse
 * Author: AI Assistant
 * Date: 1/26/2025
 * Description: Response DTO for buy statistics list with pagination
 */
@Setter
@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class StatBuyListResponse extends PageResponse {

    @Schema(description = "List of buy statistics")
    private List<StatBuyObject> statBuyObjectList;
}
