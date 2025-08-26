package com.melli.wallet.web;

import com.melli.wallet.annotation.LogExecutionTime;
import com.melli.wallet.annotation.fund_type.PurchaseTypeValidation;
import com.melli.wallet.annotation.national_code.NationalCodeValidation;
import com.melli.wallet.annotation.string.StringValidation;
import com.melli.wallet.domain.request.PanelBaseSearchJson;
import com.melli.wallet.domain.request.wallet.ActiveWalletRequestJson;
import com.melli.wallet.domain.request.wallet.CreateWalletRequestJson;
import com.melli.wallet.domain.request.wallet.DeactivatedWalletRequestJson;
import com.melli.wallet.domain.request.wallet.DeleteWalletRequestJson;
import com.melli.wallet.domain.response.base.BaseResponse;
import com.melli.wallet.domain.response.transaction.ReportTransactionResponse;
import com.melli.wallet.domain.response.transaction.StatementResponse;
import com.melli.wallet.domain.response.wallet.CreateWalletResponse;
import com.melli.wallet.domain.slave.persistence.ReportTransactionRepository;
import com.melli.wallet.exception.InternalServiceException;
import com.melli.wallet.security.RequestContext;
import com.melli.wallet.service.operation.WalletOperationalService;
import com.melli.wallet.service.repository.*;
import com.melli.wallet.service.repository.ResourceDefinition;
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
@RequestMapping("/api/v1/transaction")
@Validated
@Log4j2
public class TransactionController extends WebController {

	private final TransactionRepositoryService transactionRepositoryService;
	private final RequestContext requestContext;


	@Timed(description = "Time taken to create wallet")
	@GetMapping(path = "/last", produces = {MediaType.APPLICATION_JSON_VALUE})
	@Operation(security = { @SecurityRequirement(name = "bearer-key") },summary = "صورتحساب")
	@PreAuthorize("hasAuthority('" + ResourceDefinition.STATEMENT_AUTH + "')")
	@LogExecutionTime("Get last transaction")
	public ResponseEntity<BaseResponse<StatementResponse>> last(@Valid @NationalCodeValidation(label = "کد ملی") @RequestParam("nationalCode") String nationalCode,@Valid @RequestParam("accountNumber") @StringValidation(label = "شماره حساب کیف")  String accountNumber) throws InternalServiceException {
		String channelIp = requestContext.getClientIp();
		String username = requestContext.getChannelEntity().getUsername();
		log.info("start last wallet in username ===> {},from ip ===> {}", username, channelIp);
		StatementResponse response = transactionRepositoryService.lastTransaction(requestContext.getChannelEntity(), nationalCode, accountNumber, 100);
		return ResponseEntity.status(HttpStatus.OK).body(new BaseResponse<>(true,response));
	}


	@Timed(description = "Time taken to create wallet")
	@PostMapping(path = "/report", produces = {MediaType.APPLICATION_JSON_VALUE})
	@Operation(security = { @SecurityRequirement(name = "bearer-key") },summary = "گزارش تراکنش")
	@PreAuthorize("hasAuthority('" + ResourceDefinition.STATEMENT_AUTH + "')")
	@LogExecutionTime("Generate transaction report")
	public ResponseEntity<BaseResponse<ReportTransactionResponse>> report(@RequestBody @Validated PanelBaseSearchJson request) throws InternalServiceException {
		String channelIp = requestContext.getClientIp();
		String username = requestContext.getChannelEntity().getUsername();
		log.info("start call report transaction in username ===> {},from ip ===> {}", username, channelIp);
		ReportTransactionResponse response = transactionRepositoryService.reportTransaction(requestContext.getChannelEntity(), request.getMap());
		return ResponseEntity.status(HttpStatus.OK).body(new BaseResponse<>(true,response));
	}

}
