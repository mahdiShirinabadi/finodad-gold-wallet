package com.melli.wallet.web;

import com.melli.wallet.annotation.LogExecutionTime;
import com.melli.wallet.annotation.string.StringValidation;
import com.melli.wallet.annotation.national_code.NationalCodeValidation;
import com.melli.wallet.domain.enumaration.ResourceDefinition;
import com.melli.wallet.domain.request.wallet.ActiveWalletRequestJson;
import com.melli.wallet.domain.request.wallet.CreateWalletRequestJson;
import com.melli.wallet.domain.request.wallet.DeactivatedWalletRequestJson;
import com.melli.wallet.domain.request.wallet.DeleteWalletRequestJson;
import com.melli.wallet.domain.response.base.BaseResponse;
import com.melli.wallet.domain.response.wallet.CreateWalletResponse;
import com.melli.wallet.domain.response.wallet.TotalWalletBalanceResponse;
import com.melli.wallet.exception.InternalServiceException;
import com.melli.wallet.security.RequestContext;
import com.melli.wallet.service.operation.WalletOperationalService;
import com.melli.wallet.service.repository.*;
import com.melli.wallet.util.Utility;
import io.micrometer.core.annotation.Timed;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
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
public class WalletController extends WebController {

	private final WalletOperationalService walletOperationalService;
	private final RequestContext requestContext;


	@Timed(description = "Time taken to create wallet")
	@PostMapping(path = "/create", produces = {MediaType.APPLICATION_JSON_VALUE})
	@Operation(security = { @SecurityRequirement(name = "bearer-key") },summary = "ایجاد کیف پول")
	@PreAuthorize("hasAuthority('" + ResourceDefinition.WALLET_CREATE_AUTH + "')")
	@LogExecutionTime("Create wallet")
	public ResponseEntity<BaseResponse<CreateWalletResponse>> createWallet(@Valid  @RequestBody CreateWalletRequestJson requestJson) throws InternalServiceException {
		String channelIp = requestContext.getClientIp();
		String username = requestContext.getChannelEntity().getUsername();
		log.info("start call create wallet in username ===> {}, mobile ===> {}, from ip ===> {}", username, requestJson.getMobile(), channelIp);
		String cleanMobile = Utility.cleanPhoneNumber(requestJson.getMobile());
		CreateWalletResponse createWalletResponse = walletOperationalService.createWallet(requestContext.getChannelEntity(), cleanMobile, requestJson.getNationalCode(), WalletTypeRepositoryService.NORMAL_USER, List.of(WalletAccountCurrencyRepositoryService.GOLD, WalletAccountCurrencyRepositoryService.RIAL),
				List.of(WalletAccountTypeRepositoryService.NORMAL));
		return ResponseEntity.status(HttpStatus.OK).body(new BaseResponse<>(true,createWalletResponse));
	}



	@Timed(description = "Time taken to deactivated wallet")
	@PostMapping(path = "/deactivate", produces = {MediaType.APPLICATION_JSON_VALUE})
	@Operation(security = { @SecurityRequirement(name = "bearer-key") },summary = "غیرفعال کردن کیف پول ")
	@PreAuthorize("hasAuthority('" + ResourceDefinition.WALLET_DEACTIVATE_AUTH + "')")
	@LogExecutionTime("Deactivate wallet")
	public ResponseEntity<BaseResponse<ObjectUtils.Null>> disableWallet(@Valid @RequestBody DeactivatedWalletRequestJson requestJson) throws InternalServiceException {

		String channelIp = requestContext.getClientIp();
		log.info("start disable wallet with id ==> {}", requestJson.getId());
		walletOperationalService.deactivateWallet(requestContext.getChannelEntity() ,requestJson.getId(), channelIp);
		return ResponseEntity.status(HttpStatus.OK).body(new BaseResponse<>(true));
	}


	@Timed(description = "Time taken to delete wallet")
	@PostMapping(path = "/delete", produces = {MediaType.APPLICATION_JSON_VALUE})
	@Operation(security = { @SecurityRequirement(name = "bearer-key") },summary = "حذف کردن کیف پول ")
	@PreAuthorize("hasAuthority('" + ResourceDefinition.WALLET_DELETE_AUTH + "')")
	@LogExecutionTime("Delete wallet")
	public ResponseEntity<BaseResponse<ObjectUtils.Null>> deleteWallet(@Valid @RequestBody DeleteWalletRequestJson requestJson) throws InternalServiceException {
		String channelIp = requestContext.getClientIp();
		log.info("start delete wallet with id ==> {}", requestJson.getId());
		walletOperationalService.deleteWallet(requestContext.getChannelEntity() ,requestJson.getId(), channelIp);
		return ResponseEntity.status(HttpStatus.OK).body(new BaseResponse<>(true));
	}


	@Timed(description = "Time taken to delete wallet")
	@GetMapping(path = "/get", produces = {MediaType.APPLICATION_JSON_VALUE})
	@Operation(security = { @SecurityRequirement(name = "bearer-key") },summary = "دریافت اطلاعات کیف پول ")
	@PreAuthorize("hasAuthority('" + ResourceDefinition.WALLET_INFO_AUTH + "')")
	@LogExecutionTime("Get wallet info")
	public ResponseEntity<BaseResponse<CreateWalletResponse>> getBalance(@Valid @RequestParam @NationalCodeValidation(label = "nationalCode") String nationalCode) throws InternalServiceException {
		String channelIp = requestContext.getClientIp();
		log.info("start get wallet with nationalCode ==> {}, from Ip ({})", nationalCode, channelIp);
		CreateWalletResponse response = walletOperationalService.get(requestContext.getChannelEntity() ,nationalCode);
		return ResponseEntity.status(HttpStatus.OK).body(new BaseResponse<>(true, response));
	}


	@Timed(description = "Time taken to active wallet")
	@PostMapping(path = "/activate", produces = {MediaType.APPLICATION_JSON_VALUE})
	@Operation(security = { @SecurityRequirement(name = "bearer-key") },summary =" فعال کردن کیف پول")
	@PreAuthorize("hasAuthority('" + ResourceDefinition.WALLET_ACTIVE_AUTH + "')")
	@LogExecutionTime("Activate wallet")
	public ResponseEntity<BaseResponse<ObjectUtils.Null>> activeWallet(@Valid @RequestBody ActiveWalletRequestJson requestJson) throws InternalServiceException {

		String channelIp = requestContext.getClientIp();

		log.info("start disable wallet with mobile ==> {}", requestJson.getId());
		walletOperationalService.activateWallet(requestContext.getChannelEntity() ,requestJson.getId(), channelIp);
		return ResponseEntity.status(HttpStatus.OK).body(new BaseResponse<>(true));
	}

	@Timed(description = "Time taken to calculate total wallet balance")
	@GetMapping(path = "/totalBalance", produces = {MediaType.APPLICATION_JSON_VALUE})
	@Operation(security = { @SecurityRequirement(name = "bearer-key") },summary = "محاسبه مجموع مانده کیف‌پول‌ها")
	@PreAuthorize("hasAuthority('" + ResourceDefinition.WALLET_INFO_AUTH + "')")
	@LogExecutionTime("Calculate total wallet balance")
	public ResponseEntity<BaseResponse<TotalWalletBalanceResponse>> calculateTotalBalance(
			@Valid @StringValidation @RequestParam("currency") String currency) throws InternalServiceException {
		String channelIp = requestContext.getClientIp();
		String username = requestContext.getChannelEntity().getUsername();
		log.info("start call calculateTotalBalance in username ===> {}, currency ===> {}, from ip ===> {}", username, currency, channelIp);
		TotalWalletBalanceResponse response = walletOperationalService.calculateTotalBalance(requestContext.getChannelEntity(), currency);
		return ResponseEntity.status(HttpStatus.OK).body(new BaseResponse<>(true, response));
	}
}
