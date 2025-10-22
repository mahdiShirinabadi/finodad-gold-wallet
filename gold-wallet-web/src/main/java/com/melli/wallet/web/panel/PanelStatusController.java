package com.melli.wallet.web.panel;

import com.melli.wallet.annotation.LogExecutionTime;
import com.melli.wallet.domain.enumaration.ResourceDefinition;
import com.melli.wallet.domain.request.PanelBaseSearchJson;
import com.melli.wallet.domain.request.status.StatusCreateRequestJson;
import com.melli.wallet.domain.request.status.StatusUpdateRequestJson;
import com.melli.wallet.domain.response.base.BaseResponse;
import com.melli.wallet.domain.response.status.StatusListResponse;
import com.melli.wallet.exception.InternalServiceException;
import com.melli.wallet.security.RequestContext;
import com.melli.wallet.service.operation.StatusOperationService;
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
 * Class Name: StatusPanelController
 * Author: AI Assistant
 * Date: 1/26/2025
 * Description: Panel controller for status management operations
 */
@RestController
@RequestMapping("/api/v1/panel/status")
@RequiredArgsConstructor
@Validated
@Log4j2
public class PanelStatusController {

    private final RequestContext requestContext;
    private final StatusOperationService statusOperationService;

    @Timed(description = "Time taken to get status list")
    @PostMapping(path = "/list", produces = {MediaType.APPLICATION_JSON_VALUE})
    @Operation(security = {@SecurityRequirement(name = "bearer-key")}, summary = "لیست وضعیت‌ها", description =
            """
                            {
                              "map": {
                                "code": "0",
                                "name": "موفق",
                                "page": "0",
                                "size": "10",
                                "orderBy": "id",
                                "sort": "asc"
                              }
                            }
                     """)
    @PreAuthorize("hasAuthority('" + ResourceDefinition.STATUS_LIST_AUTH + "')")
    @LogExecutionTime("Get status list")
    public ResponseEntity<BaseResponse<StatusListResponse>> list(@RequestBody @Validated PanelBaseSearchJson request) throws InternalServiceException {
        String channelIp = requestContext.getClientIp();
        String username = requestContext.getChannelEntity().getUsername();
        log.info("start call status list in username ===> {}, from ip ===> {}", username, channelIp);
        StatusListResponse response = statusOperationService.list(requestContext.getChannelEntity(), request.getMap());
        return ResponseEntity.status(HttpStatus.OK).body(new BaseResponse<>(true, response));
    }

    @Timed(description = "Time taken to create status")
    @PostMapping(path = "/create", produces = {MediaType.APPLICATION_JSON_VALUE})
    @Operation(security = {@SecurityRequirement(name = "bearer-key")}, summary = "ایجاد وضعیت جدید")
    @PreAuthorize("hasAuthority('" + ResourceDefinition.STATUS_MANAGE_AUTH + "')")
    @LogExecutionTime("Create status")
    public ResponseEntity<BaseResponse<Object>> create(@Valid @RequestBody StatusCreateRequestJson requestJson) throws InternalServiceException {
        String channelIp = requestContext.getClientIp();
        String username = requestContext.getChannelEntity().getUsername();
        log.info("start call status create in username ===> {}, code ===> {}, persianDescription ===> {}, from ip ===> {}", username, requestJson.getCode(), requestJson.getPersianDescription(), channelIp);
        statusOperationService.create(requestContext.getChannelEntity(), requestJson.getCode(), requestJson.getPersianDescription());
        return ResponseEntity.status(HttpStatus.OK).body(new BaseResponse<>(true));
    }

    @Timed(description = "Time taken to update status")
    @PostMapping(path = "/update", produces = {MediaType.APPLICATION_JSON_VALUE})
    @Operation(security = {@SecurityRequirement(name = "bearer-key")}, summary = "ویرایش وضعیت")
    @PreAuthorize("hasAuthority('" + ResourceDefinition.STATUS_MANAGE_AUTH + "')")
    @LogExecutionTime("Update status")
    public ResponseEntity<BaseResponse<Object>> update(@Valid @RequestBody StatusUpdateRequestJson requestJson) throws InternalServiceException {
        String channelIp = requestContext.getClientIp();
        String username = requestContext.getChannelEntity().getUsername();
        log.info("start call status update in username ===> {}, id ===> {}, code ===> {}, persianDescription ===> {}, from ip ===> {}", username, requestJson.getId(), requestJson.getCode(), requestJson.getPersianDescription(), channelIp);
        statusOperationService.update(requestContext.getChannelEntity(), requestJson.getId(), requestJson.getCode(), requestJson.getPersianDescription());
        return ResponseEntity.status(HttpStatus.OK).body(new BaseResponse<>(true));
    }
}
