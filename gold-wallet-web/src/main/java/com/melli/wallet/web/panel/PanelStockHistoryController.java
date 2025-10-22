package com.melli.wallet.web.panel;

import com.melli.wallet.annotation.LogExecutionTime;
import com.melli.wallet.domain.enumaration.ResourceDefinition;
import com.melli.wallet.domain.request.PanelBaseSearchJson;
import com.melli.wallet.domain.response.base.BaseResponse;
import com.melli.wallet.domain.response.stock.StockHistoryListResponse;
import com.melli.wallet.exception.InternalServiceException;
import com.melli.wallet.security.RequestContext;
import com.melli.wallet.service.operation.StockHistoryOperationService;
import com.melli.wallet.web.WebController;
import io.micrometer.core.annotation.Timed;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * Class Name: PanelStockHistoryController
 * Author: AI Assistant
 * Date: 1/26/2025
 * Description: Panel controller for stock history management
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/panel/stockHistory")
@Validated
@Log4j2
public class PanelStockHistoryController extends WebController {

    private final StockHistoryOperationService stockHistoryOperationService;
    private final RequestContext requestContext;

    @Timed(description = "Time taken to list stock histories")
    @PostMapping(path = "/list", produces = {MediaType.APPLICATION_JSON_VALUE})
    @Operation(security = {@SecurityRequirement(name = "bearer-key")}, summary = "لیست تاریخچه سهام", description =
            """
                    {
                      "map": {
                        "stockId": "1",
                        "transactionId": "123",
                        "type": "DEPOSIT",
                        "amount": "100.00",
                        "balance": "1000.00",
                        "page": "0",
                        "size": "10",
                        "orderBy": "id",
                        "sort": "asc"
                      }
                    }
             """)
    @PreAuthorize("hasAuthority('" + ResourceDefinition.STOCK_HISTORY_LIST_AUTH + "')")
    @LogExecutionTime("List stock histories")
    public ResponseEntity<BaseResponse<StockHistoryListResponse>> list(@RequestBody @Validated PanelBaseSearchJson request) throws InternalServiceException {
        String channelIp = requestContext.getClientIp();
        String username = requestContext.getChannelEntity().getUsername();
        log.info("start call stock history list in username ===> {}, from ip ===> {}", username, channelIp);
        StockHistoryListResponse response = stockHistoryOperationService.list(requestContext.getChannelEntity(), request.getMap());
        return ResponseEntity.status(HttpStatus.OK).body(new BaseResponse<>(true, response));
    }
}
