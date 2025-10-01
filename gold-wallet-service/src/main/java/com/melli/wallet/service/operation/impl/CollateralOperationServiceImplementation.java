package com.melli.wallet.service.operation.impl;

import com.melli.wallet.domain.dto.*;
import com.melli.wallet.domain.enumaration.CollateralStatusEnum;
import com.melli.wallet.domain.master.entity.*;
import com.melli.wallet.domain.master.persistence.CreateCollateralRequestRepository;
import com.melli.wallet.domain.master.persistence.IncreaseCollateralRequestRepository;
import com.melli.wallet.domain.master.persistence.ReleaseCollateralRequestRepository;
import com.melli.wallet.domain.response.UuidResponse;
import com.melli.wallet.domain.response.collateral.CollateralListResponse;
import com.melli.wallet.domain.response.collateral.CollateralTrackResponse;
import com.melli.wallet.domain.response.collateral.CreateCollateralResponse;
import com.melli.wallet.domain.response.transaction.ReportTransactionResponse;
import com.melli.wallet.domain.response.wallet.WalletBalanceResponse;
import com.melli.wallet.domain.slave.entity.ReportTransactionEntity;
import com.melli.wallet.domain.slave.persistence.ReportTransactionRepository;
import com.melli.wallet.exception.InternalServiceException;
import com.melli.wallet.service.operation.*;
import com.melli.wallet.service.repository.*;
import com.melli.wallet.service.transactional.CollateralTransactionalService;
import com.melli.wallet.utils.Helper;
import com.melli.wallet.utils.RedisLockService;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

/**
 * Class Name: CollateralOperationServiceImplementation
 * Author: Mahdi Shirinabadi
 * Date: 9/27/2025
 */
@Service
@RequiredArgsConstructor
@Log4j2
public class CollateralOperationServiceImplementation implements CollateralOperationService {

    private final WalletOperationalService walletOperationalService;
    private final WalletAccountCurrencyRepositoryService walletAccountCurrencyRepositoryService;
    private final RrnRepositoryService rrnRepositoryService;
    private final RequestTypeRepositoryService requestTypeRepositoryService;
    private final WalletAccountRepositoryService walletAccountRepositoryService;
    private final WalletCollateralLimitationOperationService walletCollateralLimitationOperationService;
    private final RedisLockService redisLockService;
    private final RequestRepositoryService requestRepositoryService;
    private final Helper helper;
    private final WalletRepositoryService walletRepositoryService;
    private final CreateCollateralRequestRepository createCollateralRequestRepository;
    private final MessageResolverOperationService messageResolverOperationService;
    private final TemplateRepositoryService templateRepositoryService;
    private final TransactionRepositoryService transactionRepositoryService;
    private final ReleaseCollateralRequestRepository releaseCollateralRequestRepository;
    private final IncreaseCollateralRequestRepository increaseCollateralRequestRepository;
    private final StatusRepositoryService statusRepositoryService;
    private final MerchantRepositoryService merchantRepositoryService;
    private final CollateralRepositoryService collateralRepositoryService;
    private final SettingGeneralRepositoryService settingGeneralRepositoryService;
    private final ReportTransactionRepository reportTransactionRepository;
    private final CollateralTransactionalService collateralTransactionalService;
    private final CollateralHelperService collateralHelperService;
    private final GeneralValidation generalValidation;


    @Override
    public UuidResponse generateUniqueIdentifier(ChannelEntity channelEntity, String nationalCode, String quantity, String currency, String accountNumber) throws InternalServiceException {
        try {
            log.info("start generate traceId, username ===> ({}), nationalCode ({})", channelEntity.getUsername(), nationalCode);
            WalletEntity walletEntity = walletOperationalService.findUserWallet(nationalCode);
            WalletAccountCurrencyEntity currencyEntity = walletAccountCurrencyRepositoryService.findCurrency(currency);
            WalletAccountEntity walletAccountEntity = walletAccountRepositoryService.checkUserAccount(walletEntity, currencyEntity, accountNumber, nationalCode);
            walletCollateralLimitationOperationService.checkGeneral(channelEntity, walletEntity, new BigDecimal(quantity), walletAccountEntity);
            RrnEntity rrnEntity = rrnRepositoryService.generateTraceId(nationalCode, channelEntity, requestTypeRepositoryService.getRequestType(RequestTypeRepositoryService.CREATE_COLLATERAL), walletAccountEntity.getAccountNumber(), quantity);
            BalanceDTO balanceDTO = walletAccountRepositoryService.getBalance(walletAccountEntity.getId());
            if (balanceDTO.getAvailableBalance().compareTo(new BigDecimal(quantity)) <= 0) {
                log.error("balance for account ({}) is ({}) and not enough for block quantity ({})", accountNumber, balanceDTO.getAvailableBalance(), quantity);
                throw new InternalServiceException("balance not enough", StatusRepositoryService.BALANCE_IS_NOT_ENOUGH, HttpStatus.OK);
            }
            log.info("finish traceId ===> {}, username ({}), nationalCode ({})", rrnEntity.getUuid(), channelEntity.getUsername(), nationalCode);
            return new UuidResponse(rrnEntity.getUuid());
        } catch (InternalServiceException e) {
            log.error("error in generate traceId with info ===> username ({}), nationalCode ({}) error ===> ({})", channelEntity.getUsername(), nationalCode, e.getMessage());
            throw e;
        }
    }

    @Override
    public UuidResponse generateIncreaseUniqueIdentifier(ChannelEntity channelEntity, String nationalCode, String quantity, String currency, String accountNumber) throws InternalServiceException {
        try {
            log.info("start generate traceId, username ===> ({}), nationalCode ({})", channelEntity.getUsername(), nationalCode);
            WalletEntity walletEntity = walletOperationalService.findUserWallet(nationalCode);
            WalletAccountCurrencyEntity currencyEntity = walletAccountCurrencyRepositoryService.findCurrency(currency);
            WalletAccountEntity walletAccountEntity = walletAccountRepositoryService.checkUserAccount(walletEntity, currencyEntity, accountNumber, nationalCode);
            RrnEntity rrnEntity = rrnRepositoryService.generateTraceId(nationalCode, channelEntity, requestTypeRepositoryService.getRequestType(RequestTypeRepositoryService.INCREASE_COLLATERAL), walletAccountEntity.getAccountNumber(), quantity);
            BalanceDTO balanceDTO = walletAccountRepositoryService.getBalance(walletAccountEntity.getId());
            if (balanceDTO.getAvailableBalance().compareTo(new BigDecimal(quantity)) <= 0) {
                log.error("balance for account ({}) is ({}) and not enough for block quantity ({})", accountNumber, balanceDTO.getAvailableBalance(), quantity);
                throw new InternalServiceException("balance not enough", StatusRepositoryService.BALANCE_IS_NOT_ENOUGH, HttpStatus.OK);
            }
            log.info("finish traceId ===> {}, username ({}), nationalCode ({})", rrnEntity.getUuid(), channelEntity.getUsername(), nationalCode);
            return new UuidResponse(rrnEntity.getUuid());
        } catch (InternalServiceException e) {
            log.error("error in generate traceId with info ===> username ({}), nationalCode ({}) error ===> ({})", channelEntity.getUsername(), nationalCode, e.getMessage());
            throw e;
        }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public CreateCollateralResponse create(CreateCollateralObjectDTO objectDTO) throws InternalServiceException {

        log.info("=== COLLATERAL CREATE OPERATION START ===");
        log.info("Input parameters - uniqueIdentifier: {}, accountNumber: {}, quantity: {}, commission: {}, collateralId: {}", 
            objectDTO.getUniqueIdentifier(), objectDTO.getAccountNumber(), objectDTO.getQuantity(), objectDTO.getCommission(), objectDTO.getCollateralId());

        generalValidation.checkCommissionLessThanQuantity(objectDTO.getCommission(), objectDTO.getQuantity());
        log.debug("Commission validation passed - commission: {}, quantity: {}", objectDTO.getCommission(), objectDTO.getQuantity());

        RequestTypeEntity requestTypeEntity = requestTypeRepositoryService.getRequestType(RequestTypeRepositoryService.CREATE_COLLATERAL);
        log.debug("Request type retrieved - type: {}", requestTypeEntity.getName());
        
        RrnEntity rrnEntity = rrnRepositoryService.findByUid(objectDTO.getUniqueIdentifier());
        log.debug("RRN entity found - rrnId: {}, uuid: {}", rrnEntity.getId(), rrnEntity.getUuid());
        
        String key = objectDTO.getAccountNumber();
        log.info("Starting Redis lock acquisition for account: {}", key);
        
        return redisLockService.runWithLockUntilCommit(key, this.getClass(), () -> {

            log.info("=== LOCK ACQUIRED - STARTING CRITICAL SECTION ===");
            log.info("start checking existence of traceId({}) ...", objectDTO.getUniqueIdentifier());
            rrnRepositoryService.checkRrn(objectDTO.getUniqueIdentifier(), objectDTO.getChannelEntity(), requestTypeEntity, String.valueOf(objectDTO.getQuantity()), objectDTO.getAccountNumber());
            log.info("finish checking existence of traceId({})", objectDTO.getUniqueIdentifier());
            
            log.debug("Checking for duplicate collateral requests with rrnId: {}", rrnEntity.getId());
            requestRepositoryService.findCreateCollateralDuplicateWithRrnId(rrnEntity.getId());
            log.debug("No duplicate requests found");
            
            log.debug("Validating wallet and account for nationalCode: {}, accountNumber: {}", rrnEntity.getNationalCode(), objectDTO.getAccountNumber());
            WalletAccountEntity walletAccountEntity = helper.checkWalletAndWalletAccountForNormalUser(walletRepositoryService, rrnEntity.getNationalCode(), walletAccountRepositoryService, objectDTO.getAccountNumber());
            log.info("Wallet account validated - accountId: {}, accountNumber: {}", walletAccountEntity.getId(), walletAccountEntity.getAccountNumber());
            
            log.debug("Retrieving current balance for accountId: {}", walletAccountEntity.getId());
            BalanceDTO balanceDTO = walletAccountRepositoryService.getBalance(walletAccountEntity.getId());
            log.info("Current balance retrieved - available: {}, blocked: {}, total: {}", balanceDTO.getAvailable(), balanceDTO.getBlocked(), balanceDTO.getTotal());
            
            BigDecimal requiredAmount = objectDTO.getQuantity().add(objectDTO.getCommission());
            log.debug("Validating balance - required amount: {}, available balance: {}", requiredAmount, balanceDTO.getAvailable());
            generalValidation.checkBalance(balanceDTO, requiredAmount, walletAccountEntity.getAccountNumber());
            log.info("Balance validation passed - sufficient funds available");
            
            log.debug("Retrieving collateral entity with id: {}", objectDTO.getCollateralId());
            CollateralEntity collateralEntity = collateralRepositoryService.findById(Integer.parseInt(objectDTO.getCollateralId()));
            log.info("Collateral entity retrieved - id: {}, name: {}, type: {}", collateralEntity.getId(), collateralEntity.getName(), collateralEntity.getType());
            
            log.debug("Creating collateral request entity");
            CreateCollateralRequestEntity requestEntity = collateralHelperService.createCreateCollateralRequestEntity(objectDTO, walletAccountEntity, rrnEntity, requestTypeEntity, collateralEntity);
            log.info("Collateral request entity created - requestId: {}, code: {}", requestEntity.getId(), requestEntity.getCode());
            
            log.debug("Retrieving transaction templates");
            String depositTemplate = templateRepositoryService.getTemplate(TemplateRepositoryService.COLLATERAL_CREATE_DEPOSIT);
            String withdrawalTemplate = templateRepositoryService.getTemplate(TemplateRepositoryService.COLLATERAL_CREATE_WITHDRAWAL);
            log.debug("Templates retrieved - deposit: {}, withdrawal: {}", depositTemplate, withdrawalTemplate);
            
            Map<String, Object> model = new HashMap<>();
            model.put("traceId", String.valueOf(rrnEntity.getId()));
            model.put("accountNumber", walletAccountEntity.getAccountNumber());
            model.put("amount", objectDTO.getCommission());
            model.put("collateralName", collateralEntity.getName());
            log.debug("Transaction model prepared - traceId: {}, accountNumber: {}, amount: {}, collateralName: {}", 
                model.get("traceId"), model.get("accountNumber"), model.get("amount"), model.get("collateralName"));
            
            log.info("finish CreateCollateralResponse transaction for uniqueIdentifier ({}), quantity ({}) for withdrawal walletAccountId ({})", requestEntity.getRrnEntity().getUuid(), requestEntity.getQuantity(), walletAccountEntity.getId());

            if (requestEntity.getCommission().compareTo(BigDecimal.valueOf(0L)) > 0) {
                TransactionEntity commissionWithdrawal = collateralHelperService.createTransaction(walletAccountEntity, requestEntity.getCommission(),
                        messageResolverOperationService.resolve(withdrawalTemplate, model), requestEntity.getAdditionalData(), requestEntity.getRequestTypeEntity(), requestEntity.getRrnEntity());
                transactionRepositoryService.insertWithdraw(commissionWithdrawal);
                WalletAccountEntity channelCommissionAccount = walletAccountRepositoryService.findChannelCommissionAccount(objectDTO.getChannelEntity(), WalletAccountCurrencyRepositoryService.GOLD);
                log.info("start sell transaction for uniqueIdentifier ({}), commission ({}) for deposit commission from nationalCode ({}), walletAccountId ({})", requestEntity.getRrnEntity().getUuid(), requestEntity.getCommission(),
                        requestEntity.getWalletAccountEntity().getWalletEntity().getNationalCode(), channelCommissionAccount.getId());
                TransactionEntity commissionDeposit = collateralHelperService.createTransaction(channelCommissionAccount, requestEntity.getCommission(),
                        messageResolverOperationService.resolve(depositTemplate, model), requestEntity.getAdditionalData(), requestEntity.getRequestTypeEntity(), requestEntity.getRrnEntity());
                transactionRepositoryService.insertDeposit(commissionDeposit);
                log.info("finish sell transaction for uniqueIdentifier ({}), commission ({}) for deposit commission from nationalCode ({}) with transactionId ({})", requestEntity.getRrnEntity().getId(), requestEntity.getCommission(), requestEntity.getWalletAccountEntity().getWalletEntity().getNationalCode(), commissionDeposit.getId());
            }
            try {
                requestRepositoryService.save(requestEntity);
            } catch (InternalServiceException ex) {
                log.error("InternalServiceException error in save CreateCollateralRequestEntity with message ({})", ex.getMessage());
                throw ex;
            } catch (Exception ex) {
                log.error("Exception error in save CreateCollateralRequestEntity with message ({})", ex.getMessage());
                throw new InternalServiceException("error in save CreateCollateralRequestEntity", StatusRepositoryService.GENERAL_ERROR, HttpStatus.OK);
            }
            requestEntity.setResult(StatusRepositoryService.SUCCESSFUL);
            log.info("start CreateCollateralResponse transaction for uniqueIdentifier ({}), quantity ({}) for withdrawal walletAccountId ({})", requestEntity.getRrnEntity().getUuid(), requestEntity.getQuantity(), walletAccountEntity.getId());
            walletAccountRepositoryService.blockAmount(walletAccountEntity.getId(), objectDTO.getQuantity());
            // user second deposit (currency)
            requestRepositoryService.save(requestEntity);
            return helper.fillCreateCollateralResponse(requestEntity.getCode(), requestEntity.getWalletAccountEntity().getWalletEntity().getNationalCode(), requestEntity.getQuantity());
        }, objectDTO.getUniqueIdentifier());

    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void release(ReleaseCollateralObjectDTO objectDTO) throws InternalServiceException {

        generalValidation.checkCommissionLessThanQuantity(objectDTO.getCommission(), objectDTO.getQuantity());

        RequestTypeEntity requestTypeEntity = requestTypeRepositoryService.getRequestType(RequestTypeRepositoryService.RELEASE_COLLATERAL);

        String key = objectDTO.getCollateralCode();

        redisLockService.runWithLockUntilCommit(key, this.getClass(), () -> {

            log.info("start release for collateralCode ({})", key);

            Optional<CreateCollateralRequestEntity> createCollateralRequestEntityOptional = createCollateralRequestRepository.findByCode(objectDTO.getCollateralCode());

            if (createCollateralRequestEntityOptional.isEmpty()) {
                log.error("createCollateralRequestEntityOptional not found with code ({})", objectDTO.getCollateralCode());
                throw new InternalServiceException("createCollateralRequestEntityOptional not found", StatusRepositoryService.COLLATERAL_CODE_NOT_FOUND, HttpStatus.OK);
            }

            CreateCollateralRequestEntity createCollateralRequestEntity = createCollateralRequestEntityOptional.get();

            log.info("status for collateralCode ({}) is ({})", objectDTO.getCollateralCode(), createCollateralRequestEntity.getCollateralStatusEnum().toString());

            collateralHelperService.checkReleaseCollateral(createCollateralRequestEntity, objectDTO);

            WalletAccountEntity walletAccountEntity = helper.checkWalletAndWalletAccountForNormalUser(walletRepositoryService, objectDTO.getNationalCode(), walletAccountRepositoryService, createCollateralRequestEntity.getWalletAccountEntity().getAccountNumber());

            String depositTemplate = templateRepositoryService.getTemplate(TemplateRepositoryService.COLLATERAL_RELEASE_DEPOSIT);
            String withdrawalTemplate = templateRepositoryService.getTemplate(TemplateRepositoryService.COLLATERAL_RELEASE_WITHDRAWAL);

            ReleaseCollateralRequestEntity requestEntity = collateralHelperService.createReleaseCollateralRequestEntity(objectDTO, requestTypeEntity, createCollateralRequestEntity, walletAccountEntity);

            try {
                requestRepositoryService.save(requestEntity);
            } catch (Exception ex) {
                log.error("error in save ReleaseCollateral with message ({})", ex.getMessage());
                throw new InternalServiceException("error in save ReleaseCollateral", StatusRepositoryService.GENERAL_ERROR, HttpStatus.OK);
            }
            requestEntity.setResult(StatusRepositoryService.SUCCESSFUL);

            // user first withdrawal (currency)
            log.info("start ReleaseCollateral transaction for uniqueIdentifier ({}), quantity ({}) for withdrawal walletAccountId ({})", requestEntity.getRrnEntity().getUuid(), requestEntity.getQuantity(), walletAccountEntity.getId());

            int rowEffected = walletAccountRepositoryService.unblockAmount(walletAccountEntity.getId(), objectDTO.getQuantity());

            if (rowEffected != 1) {
                log.error("some error in update ReleaseCollateral for id ({}) and row update count is ({}) and sot same with 1", requestEntity.getId(), rowEffected);
                throw new InternalServiceException("some error in update CreateCollateral", StatusRepositoryService.GENERAL_ERROR, HttpStatus.OK);
            }

            Map<String, Object> model = new HashMap<>();
            model.put("traceId", String.valueOf(createCollateralRequestEntity.getRrnEntity().getId()));
            model.put("accountNumber", walletAccountEntity.getAccountNumber());
            model.put("amount", objectDTO.getCommission());

            log.info("finish ReleaseCollateralResponse transaction for uniqueIdentifier ({}), quantity ({}) for withdrawal walletAccountId ({})", requestEntity.getRrnEntity().getUuid(), requestEntity.getQuantity(), walletAccountEntity.getId());

            if (requestEntity.getCommission().compareTo(BigDecimal.valueOf(0L)) > 0) {


                TransactionEntity commissionWithdrawal = collateralHelperService.createTransaction(walletAccountEntity, requestEntity.getCommission(),
                        messageResolverOperationService.resolve(withdrawalTemplate, model), requestEntity.getAdditionalData(), requestEntity.getRequestTypeEntity(), requestEntity.getRrnEntity());
                transactionRepositoryService.insertWithdraw(commissionWithdrawal);

                WalletAccountEntity channelCommissionAccount = walletAccountRepositoryService.findChannelCommissionAccount(objectDTO.getChannelEntity(), WalletAccountCurrencyRepositoryService.GOLD);
                log.info("start collateral transaction for uniqueIdentifier ({}), commission ({}) for deposit commission from nationalCode ({}), walletAccountId ({})", requestEntity.getRrnEntity().getUuid(), requestEntity.getCommission(),
                        requestEntity.getWalletAccountEntity().getWalletEntity().getNationalCode(), channelCommissionAccount.getId());
                TransactionEntity commissionDeposit = collateralHelperService.createTransaction(channelCommissionAccount, requestEntity.getCommission(),
                        messageResolverOperationService.resolve(depositTemplate, model), requestEntity.getAdditionalData(), requestEntity.getRequestTypeEntity(), requestEntity.getRrnEntity());
                transactionRepositoryService.insertDeposit(commissionDeposit);


                log.info("finish collateral transaction for uniqueIdentifier ({}), commission ({}) for deposit commission from nationalCode ({}) with transactionId ({})", requestEntity.getRrnEntity().getId(), requestEntity.getCommission(), requestEntity.getWalletAccountEntity().getWalletEntity().getNationalCode(), commissionDeposit.getId());
            }
            // user second deposit (currency)
            requestRepositoryService.save(requestEntity);
            createCollateralRequestEntity.setCollateralStatusEnum(CollateralStatusEnum.RELEASE);
            requestRepositoryService.save(createCollateralRequestEntity);
            log.info("finish release for collateralCode ({}) and status change to ({})", objectDTO.getCollateralCode(), createCollateralRequestEntity.getCollateralStatusEnum());
            return null;
        }, key);
    }


    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void increase(IncreaseCollateralObjectDTO objectDTO) throws InternalServiceException {

        generalValidation.checkCommissionLessThanQuantity(objectDTO.getCommission(), objectDTO.getQuantity());

        RequestTypeEntity requestTypeEntity = requestTypeRepositoryService.getRequestType(RequestTypeRepositoryService.INCREASE_COLLATERAL);
        RrnEntity rrnEntity = rrnRepositoryService.findByUid(objectDTO.getUniqueIdentifier());
        String key = objectDTO.getCollateralCode();
        redisLockService.runWithLockUntilCommit(key, this.getClass(), () -> {

            Optional<CreateCollateralRequestEntity> createCollateralRequestEntityOptional = createCollateralRequestRepository.findByCode(objectDTO.getCollateralCode());
            if (createCollateralRequestEntityOptional.isEmpty()) {
                log.error("createCollateralRequestEntityOptional not found with code ({}) in increase", objectDTO.getCollateralCode());
                throw new InternalServiceException("createCollateralRequestEntityOptional not found", StatusRepositoryService.COLLATERAL_CODE_NOT_FOUND, HttpStatus.OK);
            }

            requestRepositoryService.findIncreaseCollateralDuplicateWithRrnId(rrnEntity.getId());

            CreateCollateralRequestEntity createCollateralRequestEntity = createCollateralRequestEntityOptional.get();
            collateralHelperService.checkIncreaseCollateral(createCollateralRequestEntity, objectDTO);
            WalletAccountEntity walletAccountEntity = helper.checkWalletAndWalletAccountForNormalUser(walletRepositoryService, objectDTO.getNationalCode(), walletAccountRepositoryService, createCollateralRequestEntity.getWalletAccountEntity().getAccountNumber());

            BalanceDTO balanceDTO = walletAccountRepositoryService.getBalance(walletAccountEntity.getId());
            if (balanceDTO.getAvailableBalance().compareTo(objectDTO.getQuantity().add(objectDTO.getCommission())) <= 0) {
                log.error("balance for account ({}) is ({}) and not enough for increase quantity ({})", walletAccountEntity.getAccountNumber(), balanceDTO.getAvailableBalance(), objectDTO.getQuantity());
                throw new InternalServiceException("balance not enough", StatusRepositoryService.BALANCE_IS_NOT_ENOUGH, HttpStatus.OK);
            }

            String depositTemplate = templateRepositoryService.getTemplate(TemplateRepositoryService.COLLATERAL_INCREASE_DEPOSIT);
            String withdrawalTemplate = templateRepositoryService.getTemplate(TemplateRepositoryService.COLLATERAL_INCREASE_WITHDRAWAL);

            IncreaseCollateralRequestEntity requestEntity = collateralHelperService.createIncreaseCollateralRequestEntity(objectDTO, walletAccountEntity, createCollateralRequestEntity, rrnEntity, requestTypeEntity);
            try {
                requestRepositoryService.save(requestEntity);
            } catch (Exception ex) {
                log.error("error in save IncreaseCollateral with message ({})", ex.getMessage());
                throw new InternalServiceException("error in save IncreaseCollateral", StatusRepositoryService.GENERAL_ERROR, HttpStatus.OK);
            }

            createCollateralRequestEntity.setFinalBlockQuantity(objectDTO.getQuantity().add(createCollateralRequestEntity.getQuantity()));


            // user first withdrawal (currency)
            log.info("start IncreaseCollateral transaction for uniqueIdentifier ({}), quantity ({}) for withdrawal walletAccountId ({})", requestEntity.getRrnEntity().getUuid(), requestEntity.getQuantity(), walletAccountEntity.getId());

            int rowEffected = walletAccountRepositoryService.blockAmount(walletAccountEntity.getId(), objectDTO.getQuantity());

            if (rowEffected != 1) {
                log.error("some error in update IncreaseCollateral for id ({}) and row update count is ({}) and sot same with 1", requestEntity.getId(), rowEffected);
                throw new InternalServiceException("some error in update IncreaseCollateral", StatusRepositoryService.GENERAL_ERROR, HttpStatus.OK);
            }

            Map<String, Object> model = new HashMap<>();
            model.put("traceId", String.valueOf(createCollateralRequestEntity.getRrnEntity().getId()));
            model.put("accountNumber", walletAccountEntity.getAccountNumber());
            model.put("amount", objectDTO.getCommission());

            log.info("finish IncreaseCollateralResponse transaction for uniqueIdentifier ({}), quantity ({}) for withdrawal walletAccountId ({})", requestEntity.getRrnEntity().getUuid(), requestEntity.getQuantity(), walletAccountEntity.getId());

            if (requestEntity.getCommission().compareTo(BigDecimal.valueOf(0L)) > 0) {


                TransactionEntity commissionWithdrawal = collateralHelperService.createTransaction(walletAccountEntity, requestEntity.getCommission(),
                        messageResolverOperationService.resolve(withdrawalTemplate, model), requestEntity.getAdditionalData(), requestEntity.getRequestTypeEntity(), requestEntity.getRrnEntity());
                transactionRepositoryService.insertWithdraw(commissionWithdrawal);

                WalletAccountEntity channelCommissionAccount = walletAccountRepositoryService.findChannelCommissionAccount(objectDTO.getChannelEntity(), WalletAccountCurrencyRepositoryService.GOLD);
                log.info("start increase collateral transaction for uniqueIdentifier ({}), commission ({}) for deposit commission from nationalCode ({}), walletAccountId ({})", requestEntity.getRrnEntity().getUuid(), requestEntity.getCommission(),
                        requestEntity.getWalletAccountEntity().getWalletEntity().getNationalCode(), channelCommissionAccount.getId());
                TransactionEntity commissionDeposit = collateralHelperService.createTransaction(channelCommissionAccount, requestEntity.getCommission(),
                        messageResolverOperationService.resolve(depositTemplate, model), requestEntity.getAdditionalData(), requestEntity.getRequestTypeEntity(), requestEntity.getRrnEntity());
                transactionRepositoryService.insertDeposit(commissionDeposit);


                log.info("finish increase collateral transaction for uniqueIdentifier ({}), commission ({}) for deposit commission from nationalCode ({}) with transactionId ({})", requestEntity.getRrnEntity().getId(), requestEntity.getCommission(), requestEntity.getWalletAccountEntity().getWalletEntity().getNationalCode(), commissionDeposit.getId());
            }
            // user second deposit (currency)
            requestEntity.setResult(StatusRepositoryService.SUCCESSFUL);
            requestRepositoryService.save(requestEntity);
            requestRepositoryService.save(createCollateralRequestEntity);
            return null;
        }, key);
    }

    @Override
    @Transactional
    public void seize(SeizeCollateralObjectDTO objectDTO) throws InternalServiceException {

        RequestTypeEntity requestTypeEntity = requestTypeRepositoryService.getRequestType(RequestTypeRepositoryService.SEIZE_COLLATERAL);
        String key = objectDTO.getCollateralCode();
        redisLockService.runWithLockUntilCommit(key, this.getClass(), () -> {

            Optional<CreateCollateralRequestEntity> createCollateralRequestEntityOptional = createCollateralRequestRepository.findByCode(objectDTO.getCollateralCode());
            if (createCollateralRequestEntityOptional.isEmpty()) {
                log.error("createCollateralRequestEntityOptional not found with code ({}) in seize", objectDTO.getCollateralCode());
                throw new InternalServiceException("createCollateralRequestEntityOptional not found", StatusRepositoryService.COLLATERAL_CODE_NOT_FOUND, HttpStatus.OK);
            }
            CreateCollateralRequestEntity createCollateralRequestEntity = createCollateralRequestEntityOptional.get();
            collateralHelperService.checkSeizeCollateral(createCollateralRequestEntity, objectDTO);

            WalletAccountEntity walletAccountEntity = createCollateralRequestEntity.getWalletAccountEntity();

            SeizeCollateralRequestEntity requestEntity = collateralHelperService.createSeizeCollateralRequestEntity(objectDTO, walletAccountEntity, createCollateralRequestEntity, requestTypeEntity);
            try {
                requestRepositoryService.save(requestEntity);
            } catch (Exception ex) {
                log.error("error in save LiquidCollateral with message ({})", ex.getMessage());
                throw new InternalServiceException("error in save IncreaseCollateral", StatusRepositoryService.GENERAL_ERROR, HttpStatus.OK);
            }
            //get from unblockAnd
            collateralTransactionalService.unblockAndTransfer(createCollateralRequestEntity);

            createCollateralRequestEntity.setCollateralStatusEnum(CollateralStatusEnum.SEIZE);
            requestEntity.setResult(StatusRepositoryService.SUCCESSFUL);
            requestRepositoryService.save(requestEntity);
            requestRepositoryService.save(createCollateralRequestEntity);
            return null;
        }, key);
    }


    @Transactional
    @Override
    public void sell(SellCollateralObjectDTO objectDTO) throws InternalServiceException {

        //validate input param in method we sell and remain balance move to owner collateral
        RequestTypeEntity requestTypeEntity = requestTypeRepositoryService.getRequestType(RequestTypeRepositoryService.SELL_COLLATERAL);
        String key = objectDTO.getCollateralCode();
        redisLockService.runWithLockUntilCommit(key, this.getClass(), () -> {

            Optional<CreateCollateralRequestEntity> createCollateralRequestEntityOptional = createCollateralRequestRepository.findByCode(objectDTO.getCollateralCode());
            if (createCollateralRequestEntityOptional.isEmpty()) {
                log.error("createCollateralRequestEntityOptional not found with code ({}) in sell", objectDTO.getCollateralCode());
                throw new InternalServiceException("createCollateralRequestEntityOptional not found", StatusRepositoryService.COLLATERAL_CODE_NOT_FOUND, HttpStatus.OK);
            }
            CreateCollateralRequestEntity createCollateralRequestEntity = createCollateralRequestEntityOptional.get();
            ////1 check quantity request must be less than collateral quantity
            collateralHelperService.checkSellCollateral(createCollateralRequestEntity, objectDTO);

            //2 sell quantity and fill collateral RIAL with amount from collateral
            CollateralEntity collateralEntity = createCollateralRequestEntity.getCollateralEntity();

            WalletAccountEntity collateralWalletAccountRial = walletAccountRepositoryService.findUserWalletAccount(collateralEntity.getWalletEntity(), createCollateralRequestEntity.getWalletAccountEntity().getWalletAccountCurrencyEntity(), WalletAccountCurrencyRepositoryService.RIAL);
            SellCollateralRequestEntity requestEntity = collateralHelperService.createSellCollateralRequestEntity(objectDTO, merchantRepositoryService,
                    requestTypeEntity, createCollateralRequestEntity, collateralWalletAccountRial);

            try {
                requestRepositoryService.save(requestEntity);
            } catch (Exception ex) {
                log.error("error in save SellCollateralRequestEntity with message ({})", ex.getMessage());
                throw new InternalServiceException("error in save IncreaseCollateral", StatusRepositoryService.GENERAL_ERROR, HttpStatus.OK);
            }

            collateralTransactionalService.purchaseAndCharge(requestEntity);
            collateralTransactionalService.cashout(requestEntity);
            createCollateralRequestEntity.setCollateralStatusEnum(CollateralStatusEnum.SELL);
            requestRepositoryService.save(requestEntity);
            requestRepositoryService.save(createCollateralRequestEntity);
            return null;
        }, key);
    }

    @Override
    public CollateralTrackResponse inquiry(ChannelEntity channelEntity, String uniqueIdentifier, String ip) throws InternalServiceException {
        RequestTypeEntity requestTypeEntity = requestTypeRepositoryService.getRequestType(RequestTypeRepositoryService.CREATE_COLLATERAL);
        RrnEntity rrnEntity = rrnRepositoryService.findByUid(uniqueIdentifier);
        rrnRepositoryService.checkRrn(uniqueIdentifier, channelEntity, requestTypeEntity, "", "");
        Optional<CreateCollateralRequestEntity> createCollateralRequestEntityOptional = createCollateralRequestRepository.findOptionalByRrnEntityId(rrnEntity.getId());
        if (createCollateralRequestEntityOptional.isEmpty()) {
            log.error("create collateral with rrn ({}) not found", uniqueIdentifier);
            throw new InternalServiceException("create collateral not found", StatusRepositoryService.RECORD_NOT_FOUND, HttpStatus.OK);
        }
        List<ReleaseCollateralRequestEntity> releaseCollateralRequestEntityList = releaseCollateralRequestRepository.findByCreateCollateralRequestEntity(createCollateralRequestEntityOptional.get());
        List<IncreaseCollateralRequestEntity> increaseCollateralRequestEntityList = increaseCollateralRequestRepository.findByCreateCollateralRequestEntity(createCollateralRequestEntityOptional.get());
        return helper.fillCollateralTrackResponse(createCollateralRequestEntityOptional.get(), releaseCollateralRequestEntityList, increaseCollateralRequestEntityList, statusRepositoryService);
    }

    @Override
    public WalletBalanceResponse getBalance(ChannelEntity channelEntity, String id) throws InternalServiceException {

        log.info("start get balance for merchantId ({})", id);
        CollateralEntity collateralEntity = collateralRepositoryService.findById(Integer.parseInt(id));
        if (collateralEntity == null) {
            log.error("collateralEntity {} not found", id);
            throw new InternalServiceException("merchant not found", StatusRepositoryService.MERCHANT_IS_NOT_EXIST, HttpStatus.OK);
        }
        List<WalletAccountEntity> walletAccountEntityList = walletAccountRepositoryService.findByWallet(collateralEntity.getWalletEntity());
        return helper.fillWalletBalanceResponse(walletAccountEntityList, walletAccountRepositoryService);
    }

    @Override
    public ReportTransactionResponse report(ChannelEntity channelEntity, Map<String, String> mapParameter) throws InternalServiceException {
        Pageable pageRequest = helper.getPageableConfig(settingGeneralRepositoryService,
                Integer.parseInt(Optional.ofNullable(mapParameter.get("page")).orElse("0")),
                Integer.parseInt(Optional.ofNullable(mapParameter.get("size")).orElse("10")));
        String collateralId = mapParameter.get("collateralId");
        CollateralEntity collateralEntity = collateralRepositoryService.findById(Integer.parseInt(collateralId));
        List<WalletAccountEntity> walletAccountEntityList = walletAccountRepositoryService.findByWallet(collateralEntity.getWalletEntity());
        String accountNumbersStr = String.join(",", walletAccountEntityList.stream().map(WalletAccountEntity::getAccountNumber).toList());
        mapParameter.put("walletAccountNumber", accountNumbersStr);
        Specification<ReportTransactionEntity> specification = getReportTransactionEntityPredicate(mapParameter);
        Page<ReportTransactionEntity> reportTransactionEntityPage = reportTransactionRepository.findAll(specification, pageRequest);
        return helper.fillReportStatementResponse(reportTransactionEntityPage);
    }

    @Override
    public CollateralListResponse list(ChannelEntity channelEntity, Map<String, String> mapParameter) throws InternalServiceException {
        Pageable pageRequest = helper.getPageableConfig(settingGeneralRepositoryService,
                Integer.parseInt(Optional.ofNullable(mapParameter.get("page")).orElse("0")),
                Integer.parseInt(Optional.ofNullable(mapParameter.get("size")).orElse("10")));

        Specification<CreateCollateralRequestEntity> specification = getCreateCollateralRequestEntityPredicate(mapParameter);
        Page<CreateCollateralRequestEntity> reportTransactionEntityPage = createCollateralRequestRepository.findAll(specification, pageRequest);
        return helper.fillCollateralResponse(reportTransactionEntityPage, statusRepositoryService);
    }

    private Specification<ReportTransactionEntity> getReportTransactionEntityPredicate(Map<String, String> searchCriteria) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = collateralHelperService.buildReportTransactionPredicatesFromCriteria(searchCriteria, root, criteriaBuilder);
            String orderBy = Optional.ofNullable(searchCriteria.get("orderBy")).orElse("id");
            String sortDirection = Optional.ofNullable(searchCriteria.get("sort")).orElse("asc");

            setOrder(query, orderBy, sortDirection, criteriaBuilder, root);

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    private Specification<CreateCollateralRequestEntity> getCreateCollateralRequestEntityPredicate(Map<String, String> searchCriteria) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = collateralHelperService.buildCreateCollateralPredicatesFromCriteria(searchCriteria, root, criteriaBuilder);
            String orderBy = Optional.ofNullable(searchCriteria.get("orderBy")).orElse("id");
            String sortDirection = Optional.ofNullable(searchCriteria.get("sort")).orElse("asc");

            setOrderCollateral(query, orderBy, sortDirection, criteriaBuilder, root);

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    private void setOrder(CriteriaQuery<?> query, String orderBy, String sortDirection, CriteriaBuilder criteriaBuilder,
                          Root<ReportTransactionEntity> root) {
        if ("asc".equalsIgnoreCase(sortDirection)) {
            query.orderBy(criteriaBuilder.asc(root.get(orderBy)));
        } else {
            query.orderBy(criteriaBuilder.desc(root.get(orderBy)));
        }
    }

    private void setOrderCollateral(CriteriaQuery<?> query, String orderBy, String sortDirection, CriteriaBuilder criteriaBuilder,
                                    Root<CreateCollateralRequestEntity> root) {
        if ("asc".equalsIgnoreCase(sortDirection)) {
            query.orderBy(criteriaBuilder.asc(root.get(orderBy)));
        } else {
            query.orderBy(criteriaBuilder.desc(root.get(orderBy)));
        }
    }



}
