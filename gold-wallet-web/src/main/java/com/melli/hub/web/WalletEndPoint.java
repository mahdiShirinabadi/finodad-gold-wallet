package com.melli.hub.web;

import com.melli.hub.domain.request.wallet.ActiveWalletRequestJson;
import com.melli.hub.domain.request.wallet.CreateWalletRequestJson;
import com.melli.hub.domain.request.wallet.DeactivatedWalletRequestJson;
import com.melli.hub.domain.request.wallet.DeleteWalletRequestJson;
import com.melli.hub.domain.response.base.BaseResponse;
import com.melli.hub.domain.response.wallet.CreateWalletResponse;
import com.melli.hub.exception.InternalServiceException;
import com.melli.hub.security.RequestContext;
import com.melli.hub.service.*;
import com.melli.hub.util.Utility;
import io.micrometer.core.annotation.Timed;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/wallet")
@Validated
@Log4j2
public class WalletEndPoint extends WebEndPoint {

	private final WalletOperationalService walletOperationalService;
	private final RequestContext requestContext;


	@Timed(description = "Time taken to create wallet")
	@PostMapping(path = "/create", produces = {MediaType.APPLICATION_JSON_VALUE})
	@Operation(security = { @SecurityRequirement(name = "bearer-key") },summary = "ایجاد کیف پول بدون دریافت پیغام تایید")
	@PreAuthorize("hasAuthority(\""+ ResourceService.WALLET_CREATE +"\")")
	public ResponseEntity<BaseResponse<CreateWalletResponse>> createWallet(@RequestBody CreateWalletRequestJson requestJson) throws InternalServiceException {
		String channelIp = requestContext.getClientIp();
		String username = requestContext.getChannelEntity().getUsername();
		log.info("start call create wallet in username ===> {}, mobile ===> {}, from ip ===> {}", username, requestJson.getMobile(), channelIp);
		String cleanMobile = Utility.cleanPhoneNumber(requestJson.getMobile());
		CreateWalletResponse createWalletResponse = walletOperationalService.createWallet(requestContext.getChannelEntity(), cleanMobile, requestContext.getClientIp(), WalletTypeService.NORMAL_USER, List.of(WalletAccountCurrencyService.GOLD, WalletAccountCurrencyService.RIAL),
				List.of(WalletAccountTypeService.NORMAL));
		return ResponseEntity.status(HttpStatus.OK).body(new BaseResponse<>(true,createWalletResponse));
	}



	@Timed(description = "Time taken to deactivated wallet")
	@PostMapping(path = "/deactivate", produces = {MediaType.APPLICATION_JSON_VALUE})
	@Operation(security = { @SecurityRequirement(name = "bearer-key") },summary = "غیرفعال کردن کیف پول ")
	@PreAuthorize("hasAuthority(\""+ ResourceService.WALLET_DEACTIVATE +"\")")
	public ResponseEntity<BaseResponse<ObjectUtils.Null>> disableWallet(@RequestBody DeactivatedWalletRequestJson requestJson) throws InternalServiceException {

		String channelIp = requestContext.getClientIp();
		log.info("start disable wallet with id ==> {}", requestJson.getId());
		walletOperationalService.deactivateWallet(requestContext.getChannelEntity() ,requestJson.getId(), channelIp);
		return ResponseEntity.status(HttpStatus.OK).body(new BaseResponse<>(true));
	}


	@Timed(description = "Time taken to delete wallet")
	@PostMapping(path = "/delete", produces = {MediaType.APPLICATION_JSON_VALUE})
	@Operation(security = { @SecurityRequirement(name = "bearer-key") },summary = "حذف کردن کیف پول ")
	@PreAuthorize("hasAuthority(\""+ ResourceService.WALLET_DELETE +"\")")
	public ResponseEntity<BaseResponse<ObjectUtils.Null>> deleteWallet(@RequestBody DeleteWalletRequestJson requestJson) throws InternalServiceException {
		String channelIp = requestContext.getClientIp();
		log.info("start delete wallet with id ==> {}", requestJson.getId());
		walletOperationalService.deleteWallet(requestContext.getChannelEntity() ,requestJson.getId(), channelIp);
		return ResponseEntity.status(HttpStatus.OK).body(new BaseResponse<>(true));
	}



	@Timed(description = "Time taken to active wallet")
	@PostMapping(path = "/activatedWallet", produces = {MediaType.APPLICATION_JSON_VALUE})
	@Operation(security = { @SecurityRequirement(name = "bearer-key") },summary =" فعال کردن کیف پول")
	@PreAuthorize("hasAuthority(\""+ ResourceService.WALLET_ACTIVE +"\")")
	public ResponseEntity<BaseResponse<ObjectUtils.Null>> activeWallet(@RequestBody ActiveWalletRequestJson requestJson) throws InternalServiceException {

		String channelIp = requestContext.getClientIp();

		log.info("start disable wallet with mobile ==> {}", requestJson.getId());
		walletOperationalService.activateWallet(requestContext.getChannelEntity() ,requestJson.getId(), channelIp);
		return ResponseEntity.status(HttpStatus.OK).body(new BaseResponse<>(true));
	}
}
