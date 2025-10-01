package com.melli.wallet.service.operation.impl;

import com.melli.wallet.domain.dto.*;
import com.melli.wallet.domain.enumaration.CollateralStatusEnum;
import com.melli.wallet.domain.enumaration.TransactionTypeEnum;
import com.melli.wallet.domain.master.entity.*;
import com.melli.wallet.domain.master.persistence.CreateCollateralRequestRepository;
import com.melli.wallet.domain.master.persistence.IncreaseCollateralRequestRepository;
import com.melli.wallet.domain.master.persistence.ReleaseCollateralRequestRepository;
import com.melli.wallet.domain.response.UuidResponse;
import com.melli.wallet.domain.response.collateral.CollateralListResponse;
import com.melli.wallet.domain.response.collateral.CollateralTrackResponse;
import com.melli.wallet.domain.response.collateral.CreateCollateralResponse;
import com.melli.wallet.domain.response.purchase.PurchaseResponse;
import com.melli.wallet.domain.response.transaction.ReportTransactionResponse;
import com.melli.wallet.domain.response.wallet.WalletBalanceResponse;
import com.melli.wallet.domain.slave.entity.ReportTransactionEntity;
import com.melli.wallet.domain.slave.persistence.ReportTransactionRepository;
import com.melli.wallet.exception.InternalServiceException;
import com.melli.wallet.service.operation.CollateralOperationService;
import com.melli.wallet.service.operation.MessageResolverOperationService;
import com.melli.wallet.service.operation.WalletCollateralLimitationOperationService;
import com.melli.wallet.service.operation.WalletOperationalService;
import com.melli.wallet.service.repository.*;
import com.melli.wallet.service.transactional.CollateralTransactionalService;
import com.melli.wallet.service.transactional.PurchaseTransactionalService;
import com.melli.wallet.util.StringUtils;
import com.melli.wallet.util.date.DateUtils;
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
    private final Random random = new Random();
    private final CreateCollateralRequestRepository createCollateralRequestRepository;
    private final MessageResolverOperationService messageResolverOperationService;
    private final TemplateRepositoryService templateRepositoryService;
    private final TransactionRepositoryService transactionRepositoryService;
    private final ReleaseCollateralRequestRepository releaseCollateralRequestRepository;
    private final IncreaseCollateralRequestRepository increaseCollateralRequestRepository;
    private final StatusRepositoryService statusRepositoryService;
    private final MerchantRepositoryService merchantRepositoryService;
    private final PurchaseTransactionalService purchaseTransactionalService;
    private final CollateralRepositoryService collateralRepositoryService;
    private final SettingGeneralRepositoryService settingGeneralRepositoryService;
    private final ReportTransactionRepository reportTransactionRepository;
    private final CollateralTransactionalService collateralTransactionalService;


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
            if(balanceDTO.getAvailableBalance().compareTo(new BigDecimal(quantity)) <= 0){
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
            if(balanceDTO.getAvailableBalance().compareTo(new BigDecimal(quantity)) <= 0){
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

        if ((objectDTO.getQuantity().subtract(objectDTO.getCommission())).compareTo(new BigDecimal("0")) <= 0) {
            log.error("commission ({}) is bigger than quantity ({})", objectDTO.getCommission(), objectDTO.getQuantity());
            throw new InternalServiceException("commission is bigger than quantity", StatusRepositoryService.COMMISSION_BIGGER_THAN_QUANTITY, HttpStatus.OK);
        }

        RequestTypeEntity requestTypeEntity = requestTypeRepositoryService.getRequestType(RequestTypeRepositoryService.CREATE_COLLATERAL);
        RrnEntity rrnEntity = rrnRepositoryService.findByUid(objectDTO.getUniqueIdentifier());

        String key = objectDTO.getAccountNumber();

        return redisLockService.runWithLockUntilCommit(key, this.getClass(), () -> {

            log.info("start checking existence of traceId({}) ...", objectDTO.getUniqueIdentifier());
            rrnRepositoryService.checkRrn(objectDTO.getUniqueIdentifier(), objectDTO.getChannelEntity(), requestTypeEntity, String.valueOf(objectDTO.getQuantity()), objectDTO.getAccountNumber());
            log.info("finish checking existence of traceId({})", objectDTO.getUniqueIdentifier());

            requestRepositoryService.findCreateCollateralDuplicateWithRrnId(rrnEntity.getId());


            WalletAccountEntity walletAccountEntity = helper.checkWalletAndWalletAccountForNormalUser(walletRepositoryService, rrnEntity.getNationalCode(), walletAccountRepositoryService, objectDTO.getAccountNumber());

            BalanceDTO balanceDTO = walletAccountRepositoryService.getBalance(walletAccountEntity.getId());
            if(balanceDTO.getAvailableBalance().compareTo(objectDTO.getQuantity().add(objectDTO.getCommission())) <= 0){
                log.error("balance for account ({}) is ({}) and not enough for block quantity ({})", walletAccountEntity.getAccountNumber(), balanceDTO.getAvailableBalance(), objectDTO.getQuantity());
                throw new InternalServiceException("balance not enough", StatusRepositoryService.BALANCE_IS_NOT_ENOUGH, HttpStatus.OK);
            }

            CollateralEntity collateralEntity = collateralRepositoryService.findById(Integer.parseInt(objectDTO.getCollateralId()));


            CreateCollateralRequestEntity requestEntity = new CreateCollateralRequestEntity();
            requestEntity.setCommission(objectDTO.getCommission());
            requestEntity.setQuantity(objectDTO.getQuantity());
            requestEntity.setFinalBlockQuantity(objectDTO.getQuantity());
            requestEntity.setWalletAccountEntity(walletAccountEntity);
            requestEntity.setRrnEntity(rrnEntity);
            requestEntity.setChannel(objectDTO.getChannelEntity());
            requestEntity.setResult(StatusRepositoryService.CREATE);
            requestEntity.setChannelIp(objectDTO.getIp());
            requestEntity.setRequestTypeEntity(requestTypeEntity);
            requestEntity.setCreatedBy(objectDTO.getChannelEntity().getUsername());
            requestEntity.setCreatedAt(new Date());
            requestEntity.setCollateralStatusEnum(CollateralStatusEnum.CREATE);
            requestEntity.setCode(generateCode());
            requestEntity.setAdditionalData(objectDTO.getDescription());
            requestEntity.setCollateralEntity(collateralEntity);

            String depositTemplate = templateRepositoryService.getTemplate(TemplateRepositoryService.COLLATERAL_CREATE_DEPOSIT);
            String withdrawalTemplate = templateRepositoryService.getTemplate(TemplateRepositoryService.COLLATERAL_CREATE_WITHDRAWAL);

            Map<String, Object> model = new HashMap<>();
            model.put("traceId", String.valueOf(rrnEntity.getId()));
            model.put("accountNumber", walletAccountEntity.getAccountNumber());
            model.put("amount", objectDTO.getCommission());
            model.put("collateralName", collateralEntity.getName());

            log.info("finish CreateCollateralResponse transaction for uniqueIdentifier ({}), quantity ({}) for withdrawal walletAccountId ({})", requestEntity.getRrnEntity().getUuid(), requestEntity.getQuantity(), walletAccountEntity.getId());

            if (requestEntity.getCommission().compareTo(BigDecimal.valueOf(0L)) > 0) {


                TransactionEntity commissionWithdrawal = createTransaction(walletAccountEntity, requestEntity.getCommission(),
                        messageResolverOperationService.resolve(withdrawalTemplate, model), requestEntity.getAdditionalData(), requestEntity.getRequestTypeEntity(), requestEntity.getRrnEntity());
                transactionRepositoryService.insertWithdraw(commissionWithdrawal);



                WalletAccountEntity channelCommissionAccount = walletAccountRepositoryService.findChannelCommissionAccount(objectDTO.getChannelEntity(), WalletAccountCurrencyRepositoryService.GOLD);
                log.info("start sell transaction for uniqueIdentifier ({}), commission ({}) for deposit commission from nationalCode ({}), walletAccountId ({})", requestEntity.getRrnEntity().getUuid(), requestEntity.getCommission(),
                        requestEntity.getWalletAccountEntity().getWalletEntity().getNationalCode(), channelCommissionAccount.getId());
                TransactionEntity commissionDeposit = createTransaction(channelCommissionAccount, requestEntity.getCommission(),
                        messageResolverOperationService.resolve(depositTemplate, model), requestEntity.getAdditionalData(), requestEntity.getRequestTypeEntity(), requestEntity.getRrnEntity());
                transactionRepositoryService.insertDeposit(commissionDeposit);


                log.info("finish sell transaction for uniqueIdentifier ({}), commission ({}) for deposit commission from nationalCode ({}) with transactionId ({})", requestEntity.getRrnEntity().getId(), requestEntity.getCommission(), requestEntity.getWalletAccountEntity().getWalletEntity().getNationalCode(), commissionDeposit.getId());
            }


            try {
                requestRepositoryService.save(requestEntity);
            } catch (Exception ex) {
                log.error("error in save CreateCollateralRequestEntity with message ({})", ex.getMessage());
                throw new InternalServiceException("error in save CreateCollateralRequestEntity", StatusRepositoryService.GENERAL_ERROR, HttpStatus.OK);
            }
            requestEntity.setResult(StatusRepositoryService.SUCCESSFUL);

            log.info("start CreateCollateralResponse transaction for uniqueIdentifier ({}), quantity ({}) for withdrawal walletAccountId ({})", requestEntity.getRrnEntity().getUuid(), requestEntity.getQuantity(), walletAccountEntity.getId());

            int rowEffected = walletAccountRepositoryService.blockAmount(walletAccountEntity.getId(), objectDTO.getQuantity());

            if(rowEffected != 1){
                log.error("some error in update CreateCollateral for id ({}) and row update count is ({}) and sot same with 1", requestEntity.getId(), rowEffected);
                throw new InternalServiceException("some error in update block amount CreateCollateralRequest", StatusRepositoryService.GENERAL_ERROR, HttpStatus.OK);
            }

            // user second deposit (currency)
            requestRepositoryService.save(requestEntity);

            return helper.fillCreateCollateralResponse(requestEntity.getCode(), requestEntity.getWalletAccountEntity().getWalletEntity().getNationalCode(), requestEntity.getQuantity());
        }, objectDTO.getUniqueIdentifier());

    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void release(ReleaseCollateralObjectDTO objectDTO) throws InternalServiceException {

        if ((objectDTO.getQuantity().subtract(objectDTO.getCommission())).compareTo(new BigDecimal("0")) <= 0) {
            log.error("commission release ({}) is bigger than quantity ({})", objectDTO.getCommission(), objectDTO.getQuantity());
            throw new InternalServiceException("commission is bigger than quantity", StatusRepositoryService.COMMISSION_BIGGER_THAN_QUANTITY, HttpStatus.OK);
        }

        RequestTypeEntity requestTypeEntity = requestTypeRepositoryService.getRequestType(RequestTypeRepositoryService.RELEASE_COLLATERAL);

        String key = objectDTO.getCollateralCode();

        redisLockService.runWithLockUntilCommit(key, this.getClass(), () -> {

            log.info("start release for collateralCode ({})", key);

            Optional<CreateCollateralRequestEntity> createCollateralRequestEntityOptional = createCollateralRequestRepository.findByCode(objectDTO.getCollateralCode());

            if(createCollateralRequestEntityOptional.isEmpty()) {
                log.error("createCollateralRequestEntityOptional not found with code ({})", objectDTO.getCollateralCode());
                throw new InternalServiceException("createCollateralRequestEntityOptional not found", StatusRepositoryService.COLLATERAL_CODE_NOT_FOUND, HttpStatus.OK);
            }

            CreateCollateralRequestEntity createCollateralRequestEntity = createCollateralRequestEntityOptional.get();

            log.info("status for collateralCode ({}) is ({})", objectDTO.getCollateralCode(), createCollateralRequestEntity.getCollateralStatusEnum().toString());

            checkReleaseCollateral(createCollateralRequestEntity, objectDTO);

            WalletAccountEntity walletAccountEntity = helper.checkWalletAndWalletAccountForNormalUser(walletRepositoryService, objectDTO.getNationalCode(), walletAccountRepositoryService, createCollateralRequestEntity.getWalletAccountEntity().getAccountNumber());

            String depositTemplate = templateRepositoryService.getTemplate(TemplateRepositoryService.COLLATERAL_RELEASE_DEPOSIT);
            String withdrawalTemplate = templateRepositoryService.getTemplate(TemplateRepositoryService.COLLATERAL_RELEASE_WITHDRAWAL);

            ReleaseCollateralRequestEntity requestEntity = new ReleaseCollateralRequestEntity();
            requestEntity.setCommission(objectDTO.getCommission());
            requestEntity.setQuantity(objectDTO.getQuantity());
            requestEntity.setWalletAccountEntity(walletAccountEntity);
            requestEntity.setRrnEntity(createCollateralRequestEntity.getRrnEntity());
            requestEntity.setChannel(objectDTO.getChannelEntity());
            requestEntity.setResult(StatusRepositoryService.CREATE);
            requestEntity.setChannelIp(objectDTO.getIp());
            requestEntity.setRequestTypeEntity(requestTypeEntity);
            requestEntity.setCreatedBy(objectDTO.getChannelEntity().getUsername());
            requestEntity.setCreatedAt(new Date());
            requestEntity.setAdditionalData(objectDTO.getDescription());
            requestEntity.setCreateCollateralRequestEntity(createCollateralRequestEntity);
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

            if(rowEffected != 1){
                log.error("some error in update ReleaseCollateral for id ({}) and row update count is ({}) and sot same with 1", requestEntity.getId(), rowEffected);
                throw new InternalServiceException("some error in update CreateCollateral", StatusRepositoryService.GENERAL_ERROR, HttpStatus.OK);
            }

            Map<String, Object> model = new HashMap<>();
            model.put("traceId", String.valueOf(createCollateralRequestEntity.getRrnEntity().getId()));
            model.put("accountNumber", walletAccountEntity.getAccountNumber());
            model.put("amount", objectDTO.getCommission());

            log.info("finish ReleaseCollateralResponse transaction for uniqueIdentifier ({}), quantity ({}) for withdrawal walletAccountId ({})", requestEntity.getRrnEntity().getUuid(), requestEntity.getQuantity(), walletAccountEntity.getId());

            if (requestEntity.getCommission().compareTo(BigDecimal.valueOf(0L)) > 0) {


                TransactionEntity commissionWithdrawal = createTransaction(walletAccountEntity, requestEntity.getCommission(),
                        messageResolverOperationService.resolve(withdrawalTemplate, model), requestEntity.getAdditionalData(), requestEntity.getRequestTypeEntity(), requestEntity.getRrnEntity());
                transactionRepositoryService.insertWithdraw(commissionWithdrawal);

                WalletAccountEntity channelCommissionAccount = walletAccountRepositoryService.findChannelCommissionAccount(objectDTO.getChannelEntity(), WalletAccountCurrencyRepositoryService.GOLD);
                log.info("start collateral transaction for uniqueIdentifier ({}), commission ({}) for deposit commission from nationalCode ({}), walletAccountId ({})", requestEntity.getRrnEntity().getUuid(), requestEntity.getCommission(),
                        requestEntity.getWalletAccountEntity().getWalletEntity().getNationalCode(), channelCommissionAccount.getId());
                TransactionEntity commissionDeposit = createTransaction(channelCommissionAccount, requestEntity.getCommission(),
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

        if ((objectDTO.getQuantity().subtract(objectDTO.getCommission())).compareTo(new BigDecimal("0")) <= 0) {
            log.error("commission increase ({}) is bigger than quantity ({})", objectDTO.getCommission(), objectDTO.getQuantity());
            throw new InternalServiceException("commission is bigger than quantity", StatusRepositoryService.COMMISSION_BIGGER_THAN_QUANTITY, HttpStatus.OK);
        }

        RequestTypeEntity requestTypeEntity = requestTypeRepositoryService.getRequestType(RequestTypeRepositoryService.INCREASE_COLLATERAL);
        RrnEntity rrnEntity = rrnRepositoryService.findByUid(objectDTO.getUniqueIdentifier());
        String key = objectDTO.getCollateralCode();
        redisLockService.runWithLockUntilCommit(key, this.getClass(), () -> {

            Optional<CreateCollateralRequestEntity> createCollateralRequestEntityOptional = createCollateralRequestRepository.findByCode(objectDTO.getCollateralCode());
            if(createCollateralRequestEntityOptional.isEmpty()) {
                log.error("createCollateralRequestEntityOptional not found with code ({}) in increase", objectDTO.getCollateralCode());
                throw new InternalServiceException("createCollateralRequestEntityOptional not found", StatusRepositoryService.COLLATERAL_CODE_NOT_FOUND, HttpStatus.OK);
            }

            requestRepositoryService.findIncreaseCollateralDuplicateWithRrnId(rrnEntity.getId());

            CreateCollateralRequestEntity createCollateralRequestEntity = createCollateralRequestEntityOptional.get();
            checkIncreaseCollateral(createCollateralRequestEntity, objectDTO);
            WalletAccountEntity walletAccountEntity = helper.checkWalletAndWalletAccountForNormalUser(walletRepositoryService, objectDTO.getNationalCode(), walletAccountRepositoryService, createCollateralRequestEntity.getWalletAccountEntity().getAccountNumber());

            BalanceDTO balanceDTO = walletAccountRepositoryService.getBalance(walletAccountEntity.getId());
            if(balanceDTO.getAvailableBalance().compareTo(objectDTO.getQuantity().add(objectDTO.getCommission())) <= 0){
                log.error("balance for account ({}) is ({}) and not enough for increase quantity ({})", walletAccountEntity.getAccountNumber(), balanceDTO.getAvailableBalance(), objectDTO.getQuantity());
                throw new InternalServiceException("balance not enough", StatusRepositoryService.BALANCE_IS_NOT_ENOUGH, HttpStatus.OK);
            }

            String depositTemplate = templateRepositoryService.getTemplate(TemplateRepositoryService.COLLATERAL_INCREASE_DEPOSIT);
            String withdrawalTemplate = templateRepositoryService.getTemplate(TemplateRepositoryService.COLLATERAL_INCREASE_WITHDRAWAL);

            IncreaseCollateralRequestEntity requestEntity = new IncreaseCollateralRequestEntity();
            requestEntity.setCommission(objectDTO.getCommission());
            requestEntity.setQuantity(objectDTO.getQuantity());
            requestEntity.setWalletAccountEntity(walletAccountEntity);
            requestEntity.setRrnEntity(rrnEntity);
            requestEntity.setChannel(objectDTO.getChannelEntity());
            requestEntity.setResult(StatusRepositoryService.CREATE);
            requestEntity.setChannelIp(objectDTO.getIp());
            requestEntity.setRequestTypeEntity(requestTypeEntity);
            requestEntity.setCreatedBy(objectDTO.getChannelEntity().getUsername());
            requestEntity.setCreatedAt(new Date());
            requestEntity.setAdditionalData(objectDTO.getDescription());
            requestEntity.setCreateCollateralRequestEntity(createCollateralRequestEntity);
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

            if(rowEffected != 1){
                log.error("some error in update IncreaseCollateral for id ({}) and row update count is ({}) and sot same with 1", requestEntity.getId(), rowEffected);
                throw new InternalServiceException("some error in update IncreaseCollateral", StatusRepositoryService.GENERAL_ERROR, HttpStatus.OK);
            }

            Map<String, Object> model = new HashMap<>();
            model.put("traceId", String.valueOf(createCollateralRequestEntity.getRrnEntity().getId()));
            model.put("accountNumber", walletAccountEntity.getAccountNumber());
            model.put("amount", objectDTO.getCommission());

            log.info("finish IncreaseCollateralResponse transaction for uniqueIdentifier ({}), quantity ({}) for withdrawal walletAccountId ({})", requestEntity.getRrnEntity().getUuid(), requestEntity.getQuantity(), walletAccountEntity.getId());

            if (requestEntity.getCommission().compareTo(BigDecimal.valueOf(0L)) > 0) {


                TransactionEntity commissionWithdrawal = createTransaction(walletAccountEntity, requestEntity.getCommission(),
                        messageResolverOperationService.resolve(withdrawalTemplate, model), requestEntity.getAdditionalData(), requestEntity.getRequestTypeEntity(), requestEntity.getRrnEntity());
                transactionRepositoryService.insertWithdraw(commissionWithdrawal);

                WalletAccountEntity channelCommissionAccount = walletAccountRepositoryService.findChannelCommissionAccount(objectDTO.getChannelEntity(), WalletAccountCurrencyRepositoryService.GOLD);
                log.info("start increase collateral transaction for uniqueIdentifier ({}), commission ({}) for deposit commission from nationalCode ({}), walletAccountId ({})", requestEntity.getRrnEntity().getUuid(), requestEntity.getCommission(),
                        requestEntity.getWalletAccountEntity().getWalletEntity().getNationalCode(), channelCommissionAccount.getId());
                TransactionEntity commissionDeposit = createTransaction(channelCommissionAccount, requestEntity.getCommission(),
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
            if(createCollateralRequestEntityOptional.isEmpty()) {
                log.error("createCollateralRequestEntityOptional not found with code ({}) in seize", objectDTO.getCollateralCode());
                throw new InternalServiceException("createCollateralRequestEntityOptional not found", StatusRepositoryService.COLLATERAL_CODE_NOT_FOUND, HttpStatus.OK);
            }
            CreateCollateralRequestEntity createCollateralRequestEntity = createCollateralRequestEntityOptional.get();
            checkSeizeCollateral(createCollateralRequestEntity, objectDTO);

            WalletAccountEntity walletAccountEntity = createCollateralRequestEntity.getWalletAccountEntity();

            SeizeCollateralRequestEntity requestEntity = new SeizeCollateralRequestEntity();
            requestEntity.setWalletAccountEntity(walletAccountEntity);
            requestEntity.setRrnEntity(createCollateralRequestEntity.getRrnEntity());
            requestEntity.setChannel(objectDTO.getChannelEntity());
            requestEntity.setResult(StatusRepositoryService.CREATE);
            requestEntity.setChannelIp(objectDTO.getIp());
            requestEntity.setRequestTypeEntity(requestTypeEntity);
            requestEntity.setCreatedBy(objectDTO.getChannelEntity().getUsername());
            requestEntity.setCreatedAt(new Date());
            requestEntity.setAdditionalData(objectDTO.getDescription());
            requestEntity.setCreateCollateralRequestEntity(createCollateralRequestEntity);
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
        redisLockService.runWithLockUntilCommit(key, this.getClass(), ()->{

            Optional<CreateCollateralRequestEntity> createCollateralRequestEntityOptional = createCollateralRequestRepository.findByCode(objectDTO.getCollateralCode());
            if(createCollateralRequestEntityOptional.isEmpty()) {
                log.error("createCollateralRequestEntityOptional not found with code ({}) in sell", objectDTO.getCollateralCode());
                throw new InternalServiceException("createCollateralRequestEntityOptional not found", StatusRepositoryService.COLLATERAL_CODE_NOT_FOUND, HttpStatus.OK);
            }
            CreateCollateralRequestEntity createCollateralRequestEntity = createCollateralRequestEntityOptional.get();
            ////1 check quantity request must be less than collateral quantity
            checkSellCollateral(createCollateralRequestEntity, objectDTO);

            //2 sell quantity and fill collateral RIAL with amount from collateral
            CollateralEntity collateralEntity = createCollateralRequestEntity.getCollateralEntity();

            SellCollateralRequestEntity requestEntity = new SellCollateralRequestEntity();
            requestEntity.setMerchantEntity(merchantRepositoryService.findById(Integer.parseInt(objectDTO.getMerchantId())));
            requestEntity.setCollateralWalletAccountEntity(walletAccountRepositoryService.findUserWalletAccount(collateralEntity.getWalletEntity(), createCollateralRequestEntity.getWalletAccountEntity().getWalletAccountCurrencyEntity(), WalletAccountCurrencyRepositoryService.RIAL));
            requestEntity.setPrice(Long.parseLong(objectDTO.getPrice()));
            requestEntity.setCommission(objectDTO.getCommission());
            requestEntity.setRrnEntity(createCollateralRequestEntity.getRrnEntity());
            requestEntity.setChannel(objectDTO.getChannelEntity());
            requestEntity.setResult(StatusRepositoryService.CREATE);
            requestEntity.setChannelIp(objectDTO.getIp());
            requestEntity.setRequestTypeEntity(requestTypeEntity);
            requestEntity.setCreatedBy(objectDTO.getChannelEntity().getUsername());
            requestEntity.setCreatedAt(new Date());
            requestEntity.setAdditionalData(objectDTO.getDescription());
            requestEntity.setCreateCollateralRequestEntity(createCollateralRequestEntity);
            requestEntity.setCashOutRequestEntity(null);
            requestEntity.setQuantity(objectDTO.getQuantity());
            requestEntity.setIban(createCollateralRequestEntity.getCollateralEntity().getIban());

            try {
                requestRepositoryService.save(requestEntity);
            } catch (Exception ex) {
                log.error("error in save LiquidCollateral with message ({})", ex.getMessage());
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
        if(createCollateralRequestEntityOptional.isEmpty()){
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
            List<Predicate> predicates = buildReportTransactionPredicatesFromCriteria(searchCriteria, root, criteriaBuilder);
            String orderBy = Optional.ofNullable(searchCriteria.get("orderBy")).orElse("id");
            String sortDirection = Optional.ofNullable(searchCriteria.get("sort")).orElse("asc");

            setOrder(query, orderBy, sortDirection, criteriaBuilder, root);

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    private List<Predicate> buildReportTransactionPredicatesFromCriteria(Map<String, String> searchCriteria, Root<ReportTransactionEntity> root,
                                                                         CriteriaBuilder criteriaBuilder) {
        List<Predicate> predicates = new ArrayList<>();

        String fromTime = searchCriteria.get("fromTime");
        String toTime = searchCriteria.get("toTime");
        String nationalCode = searchCriteria.get("nationalCode");
        String walletAccountNumber = searchCriteria.get("walletAccountNumber");
        String uniqueIdentifier = searchCriteria.get("uniqueIdentifier");

        if (StringUtils.hasText(searchCriteria.get("id"))) {
            predicates.add(criteriaBuilder.equal(root.get("id"), Long.parseLong(searchCriteria.get("id"))));
        }

        if (StringUtils.hasText(nationalCode)) {
            predicates.add(criteriaBuilder.equal(root.get("walletAccountEntity").get("walletEntity").get("nationalCode"), nationalCode));
        }

        if (StringUtils.hasText(walletAccountNumber)) {
            List<String> stringList = Arrays.stream(walletAccountNumber.split(",")).toList();
            predicates.add(criteriaBuilder.in(root.get("walletAccountEntity").get("accountNumber")).value(stringList));
        }

        if (StringUtils.hasText(uniqueIdentifier)) {
            predicates.add(criteriaBuilder.equal(root.get("rrnEntity").get("uuid"), uniqueIdentifier));
        }

        if ((StringUtils.hasText(fromTime))) {
            Date sDate;
            if (Integer.parseInt(fromTime.substring(0, 4)) < 1900) {
                sDate = DateUtils.parse(fromTime, DateUtils.PERSIAN_DATE_FORMAT, true, DateUtils.FARSI_LOCALE);
            } else {
                sDate = DateUtils.parse(fromTime, DateUtils.PERSIAN_DATE_FORMAT, true, DateUtils.ENGLISH_LOCALE);
            }
            predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("createdAt"), sDate));
        }

        if (StringUtils.hasText(toTime)) {
            Date tDate;
            if (Integer.parseInt(toTime.substring(0, 4)) < 1900) {
                tDate = DateUtils.parse(toTime, DateUtils.PERSIAN_DATE_FORMAT, true, DateUtils.FARSI_LOCALE);
            } else {
                tDate = DateUtils.parse(toTime, DateUtils.PERSIAN_DATE_FORMAT, true, DateUtils.ENGLISH_LOCALE);
            }
            predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("createdAt"), tDate));
        }

        return predicates;
    }

    private Specification<CreateCollateralRequestEntity> getCreateCollateralRequestEntityPredicate(Map<String, String> searchCriteria) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = buildCreateCollateralPredicatesFromCriteria(searchCriteria, root, criteriaBuilder);
            String orderBy = Optional.ofNullable(searchCriteria.get("orderBy")).orElse("id");
            String sortDirection = Optional.ofNullable(searchCriteria.get("sort")).orElse("asc");

            setOrderCollateral(query, orderBy, sortDirection, criteriaBuilder, root);

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    private List<Predicate> buildCreateCollateralPredicatesFromCriteria(Map<String, String> searchCriteria, Root<CreateCollateralRequestEntity> root,
                                                                         CriteriaBuilder criteriaBuilder) {
        List<Predicate> predicates = new ArrayList<>();

        String fromTime = searchCriteria.get("fromTime");
        String toTime = searchCriteria.get("toTime");
        String nationalCode = searchCriteria.get("nationalCode");
        String collateralId = searchCriteria.get("collateralId");

        if (StringUtils.hasText(searchCriteria.get("id"))) {
            predicates.add(criteriaBuilder.equal(root.get("id"), Long.parseLong(searchCriteria.get("id"))));
        }

        if (StringUtils.hasText(nationalCode)) {
            predicates.add(criteriaBuilder.equal(root.get("walletAccountEntity").get("walletEntity").get("nationalCode"), nationalCode));
        }


        if (StringUtils.hasText(collateralId)) {
            predicates.add(criteriaBuilder.equal(root.get("collateralEntity").get("id"), collateralId));
        }

        if ((StringUtils.hasText(fromTime))) {
            Date sDate;
            if (Integer.parseInt(fromTime.substring(0, 4)) < 1900) {
                sDate = DateUtils.parse(fromTime, DateUtils.PERSIAN_DATE_FORMAT, true, DateUtils.FARSI_LOCALE);
            } else {
                sDate = DateUtils.parse(fromTime, DateUtils.PERSIAN_DATE_FORMAT, true, DateUtils.ENGLISH_LOCALE);
            }
            predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("createdAt"), sDate));
        }

        if (StringUtils.hasText(toTime)) {
            Date tDate;
            if (Integer.parseInt(toTime.substring(0, 4)) < 1900) {
                tDate = DateUtils.parse(toTime, DateUtils.PERSIAN_DATE_FORMAT, true, DateUtils.FARSI_LOCALE);
            } else {
                tDate = DateUtils.parse(toTime, DateUtils.PERSIAN_DATE_FORMAT, true, DateUtils.ENGLISH_LOCALE);
            }
            predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("createdAt"), tDate));
        }

        return predicates;
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

    private String generateCode() {
        while (true) {
            String scratchCode = generateRandomString();
            Long countRecord = createCollateralRequestRepository.countByCode(scratchCode);
            if (countRecord == 0) {
                return scratchCode;
            }
        }
    }

    private String generateRandomString() {
        String saltChars = "ABCDEFGHJKMNLPQRSTUVWXYZ23456789$#@";
        StringBuilder salt = new StringBuilder();
        while (salt.length() < 15) { // length of the random string.
            int index = random.nextInt(saltChars.length());
            salt.append(saltChars.charAt(index));
        }
        return salt.toString();
    }

    private TransactionEntity createTransaction(WalletAccountEntity account, BigDecimal amount, String description,
                                                String additionalData, RequestTypeEntity requestTypeEntity,
                                                RrnEntity rrn) {
        TransactionEntity transaction = new TransactionEntity();
        transaction.setAmount(amount);
        transaction.setWalletAccountEntity(account);
        transaction.setDescription(description);
        transaction.setAdditionalData(additionalData);
        transaction.setRequestTypeId(requestTypeEntity.getId());
        transaction.setRrnEntity(rrn);
        return transaction;
    }


    private void checkReleaseCollateral(CreateCollateralRequestEntity createCollateralRequestEntity, ReleaseCollateralObjectDTO objectDTO) throws InternalServiceException {


        if(createCollateralRequestEntity.getChannel().getId() != objectDTO.getChannelEntity().getId()){
            log.error("owner collateral for code ({}) is ({}) and not same with channel caller ({})", createCollateralRequestEntity.getChannel().getUsername(), objectDTO.getChannelEntity().getUsername(), objectDTO.getCollateralCode());
            throw new InternalServiceException("collateral code not found", StatusRepositoryService.OWNER_COLLATERAL_CODE_SAME, HttpStatus.OK);
        }

        if(createCollateralRequestEntity.getCollateralStatusEnum().toString().equalsIgnoreCase(CollateralStatusEnum.RELEASE.toString())){
            log.error("collateral with code ({}) release before!!!", objectDTO.getCollateralCode());
            throw new InternalServiceException("collateral  release before", StatusRepositoryService.COLLATERAL_RELEASE_BEFORE, HttpStatus.OK);
        }

        if(createCollateralRequestEntity.getFinalBlockQuantity().compareTo(objectDTO.getQuantity()) != 0){
            log.error("quantity collateral with code ({}) is ({}) and not same with collateral ({})", objectDTO.getCollateralCode(), objectDTO.getQuantity(), createCollateralRequestEntity.getQuantity());
            throw new InternalServiceException("collateral quantity not same", StatusRepositoryService.COLLATERAL_QUANTITY_NOT_SAME, HttpStatus.OK);
        }

        if(!createCollateralRequestEntity.getWalletAccountEntity().getWalletEntity().getNationalCode().equalsIgnoreCase(objectDTO.getNationalCode())){
            log.error("nationalCode collateral with code ({}) is ({}) and not same with collateral ({})", objectDTO.getCollateralCode(), objectDTO.getNationalCode(), createCollateralRequestEntity.getWalletAccountEntity().getWalletEntity().getNationalCode());
            throw new InternalServiceException("collateral nationalCode not same", StatusRepositoryService.COLLATERAL_NATIONAL_CODE_NOT_SAME, HttpStatus.OK);
        }
    }

    private void checkIncreaseCollateral(CreateCollateralRequestEntity createCollateralRequestEntity, IncreaseCollateralObjectDTO objectDTO) throws InternalServiceException {


        if(createCollateralRequestEntity.getChannel().getId() != objectDTO.getChannelEntity().getId()){
            log.error("owner collateral for code ({}) is ({}) and not same with channel caller ({})", createCollateralRequestEntity.getChannel().getUsername(), objectDTO.getChannelEntity().getUsername(), objectDTO.getCollateralCode());
            throw new InternalServiceException("collateral code not found", StatusRepositoryService.OWNER_COLLATERAL_CODE_SAME, HttpStatus.OK);
        }

        if(createCollateralRequestEntity.getCollateralStatusEnum().toString().equalsIgnoreCase(CollateralStatusEnum.RELEASE.toString())){
            log.error("collateral with code ({}) release before!!!", objectDTO.getCollateralCode());
            throw new InternalServiceException("collateral  release before", StatusRepositoryService.COLLATERAL_RELEASE_BEFORE, HttpStatus.OK);
        }

        if(!createCollateralRequestEntity.getWalletAccountEntity().getWalletEntity().getNationalCode().equalsIgnoreCase(objectDTO.getNationalCode())){
            log.error("nationalCode collateral with code ({}) is ({}) and not same with collateral ({})", objectDTO.getCollateralCode(), objectDTO.getNationalCode(), createCollateralRequestEntity.getWalletAccountEntity().getWalletEntity().getNationalCode());
            throw new InternalServiceException("collateral nationalCode not same", StatusRepositoryService.COLLATERAL_NATIONAL_CODE_NOT_SAME, HttpStatus.OK);
        }
    }

    private void checkSeizeCollateral(CreateCollateralRequestEntity createCollateralRequestEntity, SeizeCollateralObjectDTO objectDTO) throws InternalServiceException {


        if(createCollateralRequestEntity.getChannel().getId() != objectDTO.getChannelEntity().getId()){
            log.error("owner collateral for code ({}) is ({}) and not same with channel caller ({}) in seize", createCollateralRequestEntity.getChannel().getUsername(), objectDTO.getChannelEntity().getUsername(), objectDTO.getCollateralCode());
            throw new InternalServiceException("collateral code not found", StatusRepositoryService.OWNER_COLLATERAL_CODE_SAME, HttpStatus.OK);
        }

        if(createCollateralRequestEntity.getCollateralStatusEnum().toString().equalsIgnoreCase(CollateralStatusEnum.RELEASE.toString())){
            log.error("collateral with code ({}) release before in seize!!!", objectDTO.getCollateralCode());
            throw new InternalServiceException("collateral release before", StatusRepositoryService.COLLATERAL_RELEASE_BEFORE, HttpStatus.OK);
        }

        if(createCollateralRequestEntity.getCollateralStatusEnum().toString().equalsIgnoreCase(CollateralStatusEnum.SEIZE.toString())){
            log.error("collateral with code ({}) release before in seize!!!", objectDTO.getCollateralCode());
            throw new InternalServiceException("collateral seize before", StatusRepositoryService.COLLATERAL_SEIZE_BEFORE, HttpStatus.OK);
        }

        if(!createCollateralRequestEntity.getWalletAccountEntity().getWalletEntity().getNationalCode().equalsIgnoreCase(objectDTO.getNationalCode())){
            log.error("nationalCode collateral with code ({}) is ({}) and not same with collateral ({})", objectDTO.getCollateralCode(), objectDTO.getNationalCode(), createCollateralRequestEntity.getWalletAccountEntity().getWalletEntity().getNationalCode());
            throw new InternalServiceException("collateral nationalCode not same", StatusRepositoryService.COLLATERAL_NATIONAL_CODE_NOT_SAME, HttpStatus.OK);
        }

    }

    private void checkSellCollateral(CreateCollateralRequestEntity createCollateralRequestEntity, SellCollateralObjectDTO objectDTO) throws InternalServiceException {


        if(createCollateralRequestEntity.getChannel().getId() != objectDTO.getChannelEntity().getId()){
            log.error("owner collateral for code ({}) is ({}) and not same with channel caller ({}) in sell", createCollateralRequestEntity.getChannel().getUsername(), objectDTO.getChannelEntity().getUsername(), objectDTO.getCollateralCode());
            throw new InternalServiceException("collateral code not found", StatusRepositoryService.OWNER_COLLATERAL_CODE_SAME, HttpStatus.OK);
        }

        if(createCollateralRequestEntity.getCollateralStatusEnum().toString().equalsIgnoreCase(CollateralStatusEnum.RELEASE.toString())){
            log.error("collateral with code ({}) release before in sell!!!", objectDTO.getCollateralCode());
            throw new InternalServiceException("collateral channel not same", StatusRepositoryService.COLLATERAL_RELEASE_BEFORE, HttpStatus.OK);
        }

        if(!createCollateralRequestEntity.getCollateralStatusEnum().toString().equalsIgnoreCase(CollateralStatusEnum.SEIZE.toString())){
            log.error("collateral with code ({}) must be SEIZE!!!", objectDTO.getCollateralCode());
            throw new InternalServiceException("collateral step not valid", StatusRepositoryService.COLLATERAL_STEP_MUST_BE_SEIZE, HttpStatus.OK);
        }

        if(!createCollateralRequestEntity.getWalletAccountEntity().getWalletEntity().getNationalCode().equalsIgnoreCase(objectDTO.getNationalCode())){
            log.error("nationalCode collateral with code ({}) is ({}) and not same with collateral sell ({})", objectDTO.getCollateralCode(), objectDTO.getNationalCode(), createCollateralRequestEntity.getWalletAccountEntity().getWalletEntity().getNationalCode());
            throw new InternalServiceException("collateral nationalCode not same", StatusRepositoryService.COLLATERAL_NATIONAL_CODE_NOT_SAME, HttpStatus.OK);
        }

        if(objectDTO.getQuantity().compareTo(createCollateralRequestEntity.getQuantity()) > 0){
            log.error("quantity for sell ({}) is bigger than first quantity in collateral ({})", objectDTO.getQuantity(), createCollateralRequestEntity.getQuantity());
            throw new InternalServiceException("quantity is bigger than input quantity", StatusRepositoryService.COLLATERAL_QUANTITY_IS_BIGGER_THAN_BLOCK_QUANTITY, HttpStatus.OK);
        }

    }

    //unblock
    public void localRelease(ReleaseCollateralObjectDTO objectDTO) throws InternalServiceException {

        if ((objectDTO.getQuantity().subtract(objectDTO.getCommission())).compareTo(new BigDecimal("0")) <= 0) {
            log.error("commission release ({}) is bigger than quantity ({})", objectDTO.getCommission(), objectDTO.getQuantity());
            throw new InternalServiceException("commission is bigger than quantity", StatusRepositoryService.COMMISSION_BIGGER_THAN_QUANTITY, HttpStatus.OK);
        }

        RequestTypeEntity requestTypeEntity = requestTypeRepositoryService.getRequestType(RequestTypeRepositoryService.RELEASE_COLLATERAL);


        String key = objectDTO.getCollateralCode();

        redisLockService.runAfterLock(key, this.getClass(), () -> {

            Optional<CreateCollateralRequestEntity> createCollateralRequestEntityOptional = createCollateralRequestRepository.findByCode(objectDTO.getCollateralCode());

            if(createCollateralRequestEntityOptional.isEmpty()) {
                log.error("createCollateralRequestEntityOptional not found with code ({})", objectDTO.getCollateralCode());
                throw new InternalServiceException("createCollateralRequestEntityOptional not found", StatusRepositoryService.COLLATERAL_CODE_NOT_FOUND, HttpStatus.OK);
            }

            CreateCollateralRequestEntity createCollateralRequestEntity = createCollateralRequestEntityOptional.get();

            checkReleaseCollateral(createCollateralRequestEntity, objectDTO);

            WalletAccountEntity walletAccountEntity = createCollateralRequestEntity.getWalletAccountEntity();

            String depositTemplate = templateRepositoryService.getTemplate(TemplateRepositoryService.COLLATERAL_RELEASE_DEPOSIT);
            String withdrawalTemplate = templateRepositoryService.getTemplate(TemplateRepositoryService.COLLATERAL_RELEASE_WITHDRAWAL);

            ReleaseCollateralRequestEntity requestEntity = new ReleaseCollateralRequestEntity();
            requestEntity.setCommission(objectDTO.getCommission());
            requestEntity.setQuantity(objectDTO.getQuantity());
            requestEntity.setWalletAccountEntity(walletAccountEntity);
            requestEntity.setRrnEntity(createCollateralRequestEntity.getRrnEntity());
            requestEntity.setChannel(objectDTO.getChannelEntity());
            requestEntity.setResult(StatusRepositoryService.CREATE);
            requestEntity.setChannelIp(objectDTO.getIp());
            requestEntity.setRequestTypeEntity(requestTypeEntity);
            requestEntity.setCreatedBy(objectDTO.getChannelEntity().getUsername());
            requestEntity.setCreatedAt(new Date());
            requestEntity.setAdditionalData(objectDTO.getDescription());
            requestEntity.setCreateCollateralRequestEntity(createCollateralRequestEntity);
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

            if(rowEffected != 1){
                log.error("some error in update ReleaseCollateral for id ({}) and row update count is ({}) and sot same with 1", requestEntity.getId(), rowEffected);
                throw new InternalServiceException("some error in update CreateCollateral", StatusRepositoryService.GENERAL_ERROR, HttpStatus.OK);
            }

            Map<String, Object> model = new HashMap<>();
            model.put("traceId", String.valueOf(createCollateralRequestEntity.getRrnEntity().getId()));
            model.put("accountNumber", walletAccountEntity.getAccountNumber());
            model.put("amount", objectDTO.getCommission());

            log.info("finish ReleaseCollateralResponse transaction for uniqueIdentifier ({}), quantity ({}) for withdrawal walletAccountId ({})", requestEntity.getRrnEntity().getUuid(), requestEntity.getQuantity(), walletAccountEntity.getId());

            if (requestEntity.getCommission().compareTo(BigDecimal.valueOf(0L)) > 0) {


                TransactionEntity commissionWithdrawal = createTransaction(walletAccountEntity, requestEntity.getCommission(),
                        messageResolverOperationService.resolve(withdrawalTemplate, model), requestEntity.getAdditionalData(), requestEntity.getRequestTypeEntity(), requestEntity.getRrnEntity());
                transactionRepositoryService.insertWithdraw(commissionWithdrawal);

                WalletAccountEntity channelCommissionAccount = walletAccountRepositoryService.findChannelCommissionAccount(objectDTO.getChannelEntity(), WalletAccountCurrencyRepositoryService.GOLD);
                log.info("start collateral transaction for uniqueIdentifier ({}), commission ({}) for deposit commission from nationalCode ({}), walletAccountId ({})", requestEntity.getRrnEntity().getUuid(), requestEntity.getCommission(),
                        requestEntity.getWalletAccountEntity().getWalletEntity().getNationalCode(), channelCommissionAccount.getId());
                TransactionEntity commissionDeposit = createTransaction(channelCommissionAccount, requestEntity.getCommission(),
                        messageResolverOperationService.resolve(depositTemplate, model), requestEntity.getAdditionalData(), requestEntity.getRequestTypeEntity(), requestEntity.getRrnEntity());
                transactionRepositoryService.insertDeposit(commissionDeposit);


                log.info("finish collateral transaction for uniqueIdentifier ({}), commission ({}) for deposit commission from nationalCode ({}) with transactionId ({})", requestEntity.getRrnEntity().getId(), requestEntity.getCommission(), requestEntity.getWalletAccountEntity().getWalletEntity().getNationalCode(), commissionDeposit.getId());
            }
            // user second deposit (currency)
            requestRepositoryService.save(requestEntity);
            createCollateralRequestEntity.setCollateralStatusEnum(CollateralStatusEnum.RELEASE);
            requestRepositoryService.save(createCollateralRequestEntity);
            return null;
        }, key);
    }

    public PurchaseResponse localSell(SellRequestDTO sellRequestDTO) throws InternalServiceException {

        if (!sellRequestDTO.getCurrency().equalsIgnoreCase(sellRequestDTO.getCommissionCurrency())) {
            log.error("commission and currency not be same!!!");
            throw new InternalServiceException("commission and currency not be same", StatusRepositoryService.COMMISSION_CURRENCY_NOT_VALID, HttpStatus.OK);
        }

        // Validate and retrieve currencies
        WalletAccountCurrencyEntity currencyEntity = walletAccountCurrencyRepositoryService.findCurrency(sellRequestDTO.getCurrency());
        WalletAccountCurrencyEntity rialCurrencyEntity = walletAccountCurrencyRepositoryService.findCurrency(WalletAccountCurrencyRepositoryService.RIAL);

        if((sellRequestDTO.getAmount().subtract(sellRequestDTO.getCommission())).compareTo(new BigDecimal("0")) <= 0){
            log.error("commission ({}) is bigger than quantity ({})", sellRequestDTO.getCommission(), sellRequestDTO.getAmount());
            throw new InternalServiceException("commission is bigger than quantity", StatusRepositoryService.COMMISSION_BIGGER_THAN_QUANTITY, HttpStatus.OK);
        }

        // Validate merchant and wallet accounts
        MerchantEntity merchant = merchantRepositoryService.findMerchant(sellRequestDTO.getMerchantId());

        if(merchant.getStatus() == MerchantRepositoryService.DISABLED){
            log.error("merchant is disable and system can not buy any things");
            throw new InternalServiceException("merchant is disable", StatusRepositoryService.MERCHANT_IS_DISABLE, HttpStatus.OK);
        }

        WalletAccountEntity merchantCurrencyAccount = merchantRepositoryService.findMerchantWalletAccount(merchant, currencyEntity);
        WalletAccountEntity merchantRialAccount = merchantRepositoryService.findMerchantWalletAccount(merchant, rialCurrencyEntity);

        // Validate user and wallet accounts
        WalletEntity userWallet = walletOperationalService.findUserWallet(sellRequestDTO.getNationalCode());
        WalletAccountEntity userRialAccount = walletAccountRepositoryService.findUserWalletAccount(userWallet, rialCurrencyEntity, sellRequestDTO.getCurrency());
        WalletAccountEntity userCurrencyAccount = walletAccountRepositoryService.checkUserAccount(userWallet, currencyEntity, sellRequestDTO.getWalletAccountNumber(), sellRequestDTO.getNationalCode());

        // Validate channel commission account
        WalletAccountEntity channelCommissionAccount = walletAccountRepositoryService.findChannelCommissionAccount(sellRequestDTO.getChannel(), sellRequestDTO.getCommissionCurrency());

        RrnEntity rrnEntity = rrnRepositoryService.generateTraceId(sellRequestDTO.getNationalCode(), sellRequestDTO.getChannel(),
                requestTypeRepositoryService.getRequestType(TransactionTypeEnum.SELL.name()), sellRequestDTO.getWalletAccountNumber(), String.valueOf(sellRequestDTO.getAmount()));

        return redisLockService.runAfterLock(sellRequestDTO.getWalletAccountNumber(), this.getClass(), () -> purchaseTransactionalService.processSell(new PurchaseObjectDto(
                sellRequestDTO.getChannel(),
                rrnEntity.getUuid(),
                sellRequestDTO.getAmount(),
                BigDecimal.valueOf(sellRequestDTO.getPrice()),
                sellRequestDTO.getCommission(),
                sellRequestDTO.getAdditionalData(),
                sellRequestDTO.getNationalCode(),
                userWallet,
                userRialAccount,
                userCurrencyAccount,
                merchant,
                merchantRialAccount,
                merchantCurrencyAccount,
                channelCommissionAccount)
        ), sellRequestDTO.getNationalCode());
    }


}
