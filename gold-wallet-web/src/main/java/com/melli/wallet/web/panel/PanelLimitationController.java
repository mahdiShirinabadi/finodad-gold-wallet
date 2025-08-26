package com.melli.wallet.web.panel;

import com.melli.wallet.annotation.LogExecutionTime;
import com.melli.wallet.domain.request.PanelBaseSearchJson;
import com.melli.wallet.domain.request.limitation.CreateLimitationGeneralCustomRequestJson;
import com.melli.wallet.domain.request.limitation.UpdateLimitationGeneralRequestJson;
import com.melli.wallet.domain.response.base.BaseResponse;
import com.melli.wallet.domain.response.limitation.GeneralCustomLimitationListResponse;
import com.melli.wallet.domain.response.limitation.GeneralLimitationListResponse;
import com.melli.wallet.exception.InternalServiceException;
import com.melli.wallet.security.RequestContext;
import com.melli.wallet.service.repository.LimitationGeneralCustomRepositoryService;
import com.melli.wallet.service.repository.LimitationGeneralService;
import com.melli.wallet.service.operation.LimitationOperationService;
import com.melli.wallet.service.repository.ResourceRepositoryService;
import com.melli.wallet.service.repository.ResourceDefinition;
import com.melli.wallet.util.Utility;
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
 * Class Name: PanelLimitationController
 * Author: Mahdi Shirinabadi
 * Date: 7/21/2025
 */
@RestController
@RequiredArgsConstructor
@RequestMapping(value = {"/api/v1/panel/limitation"})
@Validated
@Log4j2
public class PanelLimitationController extends WebController {

    private final RequestContext requestContext;
    private final LimitationGeneralService limitationGeneralService;
    private final LimitationGeneralCustomRepositoryService limitationGeneralCustomRepositoryService;
    private final LimitationOperationService limitationOperationService;

    @Timed(description = "Time taken to update limitation general")
    @PostMapping(path = "/updateGeneral", produces = {MediaType.APPLICATION_JSON_VALUE}, consumes = {MediaType.APPLICATION_JSON_VALUE})
    @Operation(security = { @SecurityRequirement(name = "bearer-key") },summary = "Update general limitation (value and pattern only)")
    @PreAuthorize("hasAuthority('" + ResourceDefinition.LIMITATION_MANAGE_AUTH + "')")
    @LogExecutionTime("Update limitation general")
    public ResponseEntity<BaseResponse<String>> updateLimitationGeneral(@Valid @RequestBody UpdateLimitationGeneralRequestJson request) throws InternalServiceException {
        String channelIp = requestContext.getClientIp();
        String username = requestContext.getChannelEntity().getUsername();
        log.info("start update limitation general in username ===> {}, limitationName ===> {}, from ip ===> {}", username, request.getId(), channelIp);
        limitationOperationService.updateLimitationGeneral(Long.parseLong(request.getId()), request.getValue(), request.getPattern() , requestContext.getChannelEntity());
        return ResponseEntity.status(HttpStatus.OK).body(new BaseResponse<>(true, "General limitation updated successfully"));
    }

    @Timed(description = "Time taken to insert limitation general custom")
    @PostMapping(path = "/insertGeneralCustom", produces = {MediaType.APPLICATION_JSON_VALUE}, consumes = {MediaType.APPLICATION_JSON_VALUE})
    @Operation(security = { @SecurityRequirement(name = "bearer-key") },summary = "Insert general custom limitation")
    @PreAuthorize("hasAuthority('" + ResourceDefinition.LIMITATION_MANAGE_AUTH + "')")
    @LogExecutionTime("Insert limitation general custom")
    public ResponseEntity<BaseResponse<String>> insertLimitationGeneralCustom(@Valid @RequestBody CreateLimitationGeneralCustomRequestJson request) throws InternalServiceException {
        String channelIp = requestContext.getClientIp();
        String username = requestContext.getChannelEntity().getUsername();
        log.info("start insert limitation general custom in username ===> {}, limitationId ===> {}, from ip ===> {}", username, request.getLimitationGeneralId(), channelIp);
        limitationOperationService.insertLimitationGeneralCustom(
                Long.parseLong(request.getLimitationGeneralId()),
                request.getValue(),
                request.getAdditionalData(),
                request.getWalletLevelId() != null ? Long.parseLong(request.getWalletLevelId()) : null,
                request.getWalletAccountTypeId() != null ? Long.parseLong(request.getWalletAccountTypeId()) : null,
                request.getWalletAccountCurrencyId() != null ? Long.parseLong(request.getWalletAccountCurrencyId()) : null,
                request.getWalletTypeId() != null ? Long.parseLong(request.getWalletTypeId()) : null,
                request.getChannelId() != null ? Long.parseLong(request.getChannelId()) : null,
                requestContext.getChannelEntity()
        );
        return ResponseEntity.status(HttpStatus.OK).body(new BaseResponse<>(true, "General custom limitation inserted successfully"));
    }

    @Timed(description = "Time taken to get general limitations list")
    @PostMapping(path = "/generalList", produces = {MediaType.APPLICATION_JSON_VALUE})
    @Operation(security = { @SecurityRequirement(name = "bearer-key") },summary = "Get general limitations list")
    @PreAuthorize("hasAuthority('" + ResourceDefinition.LIMITATION_MANAGE_AUTH + "')")
    @LogExecutionTime("Get general limitations list")
    public ResponseEntity<BaseResponse<GeneralLimitationListResponse>> getGeneralLimitationsList(@Valid @RequestBody PanelBaseSearchJson panelSearchJson) throws InternalServiceException {
        String channelIp = requestContext.getClientIp();
        String username = requestContext.getChannelEntity().getUsername();
        log.info("start get generalList by username ({}), ip ({}) for request ({})", username,
                channelIp, Utility.mapToJsonOrNull(panelSearchJson));
        log.info("start get general limitations list in username ===> {}, from ip ===> {}", username, channelIp);
        GeneralLimitationListResponse response = limitationGeneralService.getGeneralLimitationList(requestContext.getChannelEntity(), panelSearchJson.getMap());
        return ResponseEntity.status(HttpStatus.OK).body(new BaseResponse<>(true, response));
    }

    @Timed(description = "Time taken to get general custom limitations list")
    @PostMapping(path = "/generalCustomList", produces = {MediaType.APPLICATION_JSON_VALUE})
    @Operation(security = { @SecurityRequirement(name = "bearer-key") },summary = "Get general custom limitations list")
    @PreAuthorize("hasAuthority('" + ResourceDefinition.LIMITATION_MANAGE_AUTH + "')")
    @LogExecutionTime("Get general custom limitations list")
    public ResponseEntity<BaseResponse<GeneralCustomLimitationListResponse>> getGeneralCustomLimitationsList(@Valid @RequestBody PanelBaseSearchJson panelSearchJson) throws InternalServiceException {
        String channelIp = requestContext.getClientIp();
        String username = requestContext.getChannelEntity().getUsername();
        log.info("start get generalCustomList by username ({}), ip ({}) for request ({})", username,
                channelIp, Utility.mapToJsonOrNull(panelSearchJson));
        log.info("start get general custom limitations list in username ===> {}, from ip ===> {}", username, channelIp);
        GeneralCustomLimitationListResponse response = limitationGeneralCustomRepositoryService.getGeneralCustomLimitationList(requestContext.getChannelEntity(), panelSearchJson.getMap());
        return ResponseEntity.status(HttpStatus.OK).body(new BaseResponse<>(true, response));
    }

}
