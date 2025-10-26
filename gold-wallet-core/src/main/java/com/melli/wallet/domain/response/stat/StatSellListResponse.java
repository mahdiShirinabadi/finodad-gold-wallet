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
 * Class Name: StatSellListResponse
 * Author: AI Assistant
 * Date: 1/26/2025
 * Description: Response DTO for sell statistics list with pagination
 */
@Setter
@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class StatSellListResponse extends PageResponse {

    @Schema(description = "List of sell statistics")
    private List<StatSellObject> statSellObjectList;
}
