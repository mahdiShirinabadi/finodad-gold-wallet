package com.melli.wallet.web.panel;

import com.melli.wallet.annotation.LogExecutionTime;
import com.melli.wallet.domain.enumaration.ResourceDefinition;
import com.melli.wallet.domain.request.PanelBaseSearchJson;
import com.melli.wallet.domain.response.base.BaseResponse;
import com.melli.wallet.domain.response.stock.StockListResponse;
import com.melli.wallet.exception.InternalServiceException;
import com.melli.wallet.security.RequestContext;
import com.melli.wallet.service.operation.StockOperationService;
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
 * Class Name: PanelStockController
 * Author: AI Assistant
 * Date: 1/26/2025
 * Description: Panel controller for stock management
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/panel/stock")
@Validated
@Log4j2
public class PanelStockController extends WebController {

    private final StockOperationService stockOperationService;
    private final RequestContext requestContext;

    @Timed(description = "Time taken to list stocks")
    @PostMapping(path = "/list", produces = {MediaType.APPLICATION_JSON_VALUE})
    @Operation(security = {@SecurityRequirement(name = "bearer-key")}, summary = "لیست سهام", description =
            """
                    {
                      "map": {
                        "code": "01",
                        "name": "سهام",
                        "walletAccountCurrencyId": "1",
                        "page": "0",
                        "size": "10",
                        "orderBy": "id",
                        "sort": "asc"
                      }
                    }
             """)
    @PreAuthorize("hasAuthority('" + ResourceDefinition.STOCK_LIST_AUTH + "')")
    @LogExecutionTime("List stocks")
    public ResponseEntity<BaseResponse<StockListResponse>> list(@RequestBody @Validated PanelBaseSearchJson request) throws InternalServiceException {
        String channelIp = requestContext.getClientIp();
        String username = requestContext.getChannelEntity().getUsername();
        log.info("start call stock list in username ===> {}, from ip ===> {}", username, channelIp);
        StockListResponse response = stockOperationService.list(requestContext.getChannelEntity(), request.getMap());
        return ResponseEntity.status(HttpStatus.OK).body(new BaseResponse<>(true, response));
    }
}
