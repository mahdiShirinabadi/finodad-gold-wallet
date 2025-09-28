package com.melli.wallet.service.operation.impl;

import com.melli.wallet.domain.dto.BalanceDTO;
import com.melli.wallet.domain.dto.CreateCollateralObjectDTO;
import com.melli.wallet.domain.dto.ReleaseCollateralObjectDTO;
import com.melli.wallet.domain.enumaration.CollateralStatusEnum;
import com.melli.wallet.domain.master.entity.*;
import com.melli.wallet.domain.master.persistence.CreateCollateralRequestRepository;
import com.melli.wallet.domain.master.persistence.ReleaseCollateralRequestRepository;
import com.melli.wallet.domain.response.UuidResponse;
import com.melli.wallet.domain.response.collateral.CreateCollateralResponse;
import com.melli.wallet.exception.InternalServiceException;
import com.melli.wallet.service.operation.CollateralOperationService;
import com.melli.wallet.service.operation.MessageResolverOperationService;
import com.melli.wallet.service.operation.WalletCollateralLimitationOperationService;
import com.melli.wallet.service.operation.WalletOperationalService;
import com.melli.wallet.service.repository.*;
import com.melli.wallet.utils.Helper;
import com.melli.wallet.utils.RedisLockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
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
    @Transactional
    public CreateCollateralResponse create(CreateCollateralObjectDTO objectDTO) throws InternalServiceException {

        if ((objectDTO.getQuantity().subtract(objectDTO.getCommission())).compareTo(new BigDecimal("0")) <= 0) {
            log.error("commission ({}) is bigger than quantity ({})", objectDTO.getCommission(), objectDTO.getQuantity());
            throw new InternalServiceException("commission is bigger than quantity", StatusRepositoryService.COMMISSION_BIGGER_THAN_QUANTITY, HttpStatus.OK);
        }

        RequestTypeEntity requestTypeEntity = requestTypeRepositoryService.getRequestType(RequestTypeRepositoryService.CREATE_COLLATERAL);
        RrnEntity rrnEntity = rrnRepositoryService.findByUid(objectDTO.getUniqueIdentifier());

        String key = objectDTO.getAccountNumber();

        return redisLockService.runAfterLock(key, this.getClass(), () -> {

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


            CreateCollateralRequestEntity requestEntity = new CreateCollateralRequestEntity();
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
            requestEntity.setCollateralStatusEnum(CollateralStatusEnum.CREATE);
            requestEntity.setCode(generateCode());
            requestEntity.setAdditionalData(objectDTO.getDescription());

            String depositTemplate = templateRepositoryService.getTemplate(TemplateRepositoryService.COLLATERAL_CREATE_DEPOSIT);
            String withdrawalTemplate = templateRepositoryService.getTemplate(TemplateRepositoryService.COLLATERAL_CREATE_WITHDRAWAL);

            Map<String, Object> model = new HashMap<>();
            model.put("traceId", String.valueOf(rrnEntity.getId()));
            model.put("accountNumber", walletAccountEntity.getAccountNumber());
            model.put("amount", objectDTO.getCommission());


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

            // user first withdrawal (currency)
            log.info("start CreateCollateralResponse transaction for uniqueIdentifier ({}), quantity ({}) for withdrawal walletAccountId ({})", requestEntity.getRrnEntity().getUuid(), requestEntity.getQuantity(), walletAccountEntity.getId());

            int rowEffected = walletAccountRepositoryService.blockAmount(walletAccountEntity.getId(), objectDTO.getQuantity());

            if(rowEffected != 1){
                log.error("some error in update CreateCollateral for id ({}) and row update count is ({}) and sot same with 1", requestEntity.getId(), rowEffected);
                throw new InternalServiceException("some error in update CreateCollateral", StatusRepositoryService.GENERAL_ERROR, HttpStatus.OK);
            }


            // user second deposit (currency)
            requestRepositoryService.save(requestEntity);

            return helper.fillCreateCollateralResponse(requestEntity.getCode(), requestEntity.getWalletAccountEntity().getWalletEntity().getNationalCode(), requestEntity.getQuantity());
        }, objectDTO.getUniqueIdentifier());

    }

    @Override
    public void inquiry(ChannelEntity channelEntity, String uniqueIdentifier) throws InternalServiceException{
        RequestTypeEntity requestTypeEntity = requestTypeRepositoryService.getRequestType(RequestTypeRepositoryService.CREATE_COLLATERAL);
        RrnEntity rrnEntity = rrnRepositoryService.findByUid(uniqueIdentifier);
        rrnRepositoryService.checkRrn(uniqueIdentifier, channelEntity, requestTypeEntity, "", "");
        CreateCollateralRequestEntity createCollateralRequestEntity = createCollateralRequestRepository.findByRrnEntityId(rrnEntity.getId());
        List<ReleaseCollateralRequestEntity> releaseCollateralRequestEntityList = releaseCollateralRequestRepository.findByCreateCollateralRequestEntity(createCollateralRequestEntity);
    }

    @Override
    @Transactional
    public void release(ReleaseCollateralObjectDTO objectDTO) throws InternalServiceException {

        if ((objectDTO.getQuantity().subtract(objectDTO.getCommission())).compareTo(new BigDecimal("0")) <= 0) {
            log.error("commission ({}) is bigger than quantity ({})", objectDTO.getCommission(), objectDTO.getQuantity());
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
                log.info("start sell transaction for uniqueIdentifier ({}), commission ({}) for deposit commission from nationalCode ({}), walletAccountId ({})", requestEntity.getRrnEntity().getUuid(), requestEntity.getCommission(),
                        requestEntity.getWalletAccountEntity().getWalletEntity().getNationalCode(), channelCommissionAccount.getId());
                TransactionEntity commissionDeposit = createTransaction(channelCommissionAccount, requestEntity.getCommission(),
                        messageResolverOperationService.resolve(depositTemplate, model), requestEntity.getAdditionalData(), requestEntity.getRequestTypeEntity(), requestEntity.getRrnEntity());
                transactionRepositoryService.insertDeposit(commissionDeposit);


                log.info("finish sell transaction for uniqueIdentifier ({}), commission ({}) for deposit commission from nationalCode ({}) with transactionId ({})", requestEntity.getRrnEntity().getId(), requestEntity.getCommission(), requestEntity.getWalletAccountEntity().getWalletEntity().getNationalCode(), commissionDeposit.getId());
            }
            // user second deposit (currency)
            requestRepositoryService.save(requestEntity);
            createCollateralRequestEntity.setCollateralStatusEnum(CollateralStatusEnum.RELEASE);
            requestRepositoryService.save(createCollateralRequestEntity);
            return null;
        }, key);
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
            throw new InternalServiceException("collateral channel not same", StatusRepositoryService.COLLATERAL_RELEASE_BEFORE, HttpStatus.OK);
        }

        if(createCollateralRequestEntity.getQuantity().compareTo(objectDTO.getQuantity()) != 0){
            log.error("quantity collateral with code ({}) is ({}) and not same with collateral ({})", objectDTO.getCollateralCode(), objectDTO.getQuantity(), createCollateralRequestEntity.getQuantity());
            throw new InternalServiceException("collateral quantity not same", StatusRepositoryService.COLLATERAL_QUANTITY_NOT_SAME, HttpStatus.OK);
        }

        if(!createCollateralRequestEntity.getWalletAccountEntity().getWalletEntity().getNationalCode().equalsIgnoreCase(objectDTO.getNationalCode())){
            log.error("nationalCode collateral with code ({}) is ({}) and not same with collateral ({})", objectDTO.getCollateralCode(), objectDTO.getNationalCode(), createCollateralRequestEntity.getWalletAccountEntity().getWalletEntity().getNationalCode());
            throw new InternalServiceException("collateral nationalCode not same", StatusRepositoryService.COLLATERAL_NATIONAL_CODE_NOT_SAME, HttpStatus.OK);
        }
    }
}
