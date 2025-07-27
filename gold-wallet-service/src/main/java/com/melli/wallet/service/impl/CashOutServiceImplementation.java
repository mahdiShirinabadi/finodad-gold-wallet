package com.melli.wallet.service.impl;

import com.melli.wallet.domain.dto.CashOutObjectDTO;
import com.melli.wallet.domain.dto.PhysicalCashOutObjectDTO;
import com.melli.wallet.domain.master.entity.*;
import com.melli.wallet.domain.response.UuidResponse;
import com.melli.wallet.domain.response.cash.CashOutResponse;
import com.melli.wallet.domain.response.cash.CashOutTrackResponse;
import com.melli.wallet.domain.response.cash.PhysicalCashOutResponse;
import com.melli.wallet.domain.response.cash.PhysicalCashOutTrackResponse;
import com.melli.wallet.exception.InternalServiceException;
import com.melli.wallet.service.*;
import com.melli.wallet.util.Utility;
import com.melli.wallet.utils.Helper;
import com.melli.wallet.utils.RedisLockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class Name: CashOutServiceImplementation
 * Author: Mahdi Shirinabadi
 * Date: 5/3/2025
 */
@Service
@Log4j2
@RequiredArgsConstructor
public class CashOutServiceImplementation implements CashOutService {

    private final RedisLockService redisLockService;
    private final RrnService rrnService;
    private final RequestService requestService;
    private final Helper helper;
    private final WalletService walletService;
    private final WalletAccountService walletAccountService;
    private final WalletCashLimitationService walletCashLimitationService;
    private final RequestTypeService requestTypeService;
    private final TemplateService templateService;
    private final TransactionService transactionService;
    private final MessageResolverService messageResolverService;
    private final StatusService statusService;
    private final WalletAccountTypeService walletAccountTypeService;

    @Override
    public UuidResponse generateUuid(ChannelEntity channelEntity, String nationalCode, String amount, String accountNumber) throws InternalServiceException {
        try {
            WalletAccountEntity walletAccountEntity = helper.checkWalletAndWalletAccountForNormalUser(walletService, nationalCode, walletAccountService, accountNumber);
            walletCashLimitationService.checkCashOutLimitation(channelEntity, walletAccountEntity.getWalletEntity(), BigDecimal.valueOf(Long.parseLong(amount)), walletAccountEntity);
            log.info("start generate traceId, username ===> ({}), nationalCode ({})", channelEntity.getUsername(), nationalCode);
            RrnEntity rrnEntity = rrnService.generateTraceId(nationalCode, channelEntity, requestTypeService.getRequestType(RequestTypeService.CASH_OUT), accountNumber, amount);
            log.info("finish traceId ===> {}, username ({}), nationalCode ({})", rrnEntity.getUuid(), channelEntity.getUsername(), nationalCode);
            return new UuidResponse(rrnEntity.getUuid());
        } catch (InternalServiceException e) {
            log.error("error in generate traceId with info ===> username ({}), nationalCode ({}) error ===> ({})", channelEntity.getUsername(), nationalCode, e.getMessage());
            throw e;
        }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public CashOutResponse withdrawal(CashOutObjectDTO cashOutObjectDTO) throws InternalServiceException {

        RequestTypeEntity requestTypeEntity = requestTypeService.getRequestType(RequestTypeService.CASH_OUT);
        RrnEntity rrnEntity = rrnService.findByUid(cashOutObjectDTO.getUniqueIdentifier());

        return redisLockService.runAfterLock(cashOutObjectDTO.getAccountNumber(), this.getClass(), () -> {
            log.info("start checking existence of traceId({}) ...", cashOutObjectDTO.getUniqueIdentifier());
            rrnService.checkRrn(cashOutObjectDTO.getUniqueIdentifier(), cashOutObjectDTO.getChannel(), requestTypeEntity, String.valueOf(cashOutObjectDTO.getAmount()), cashOutObjectDTO.getAccountNumber());
            log.info("finish checking existence of traceId({})", cashOutObjectDTO.getUniqueIdentifier());

            requestService.findCashOutDuplicateWithRrnId(rrnEntity.getId());

            WalletAccountEntity walletAccountEntity = helper.checkWalletAndWalletAccountForNormalUser(walletService, rrnEntity.getNationalCode(), walletAccountService, cashOutObjectDTO.getAccountNumber());
            walletCashLimitationService.checkCashOutLimitation(cashOutObjectDTO.getChannel(), walletAccountEntity.getWalletEntity(), BigDecimal.valueOf(Long.parseLong(cashOutObjectDTO.getAmount())), walletAccountEntity);

            CashOutRequestEntity cashOutRequestEntity = new CashOutRequestEntity();
            cashOutRequestEntity.setAmount(Long.parseLong(cashOutObjectDTO.getAmount()));
            cashOutRequestEntity.setIban(cashOutObjectDTO.getIban());
            cashOutRequestEntity.setWalletAccountEntity(walletAccountEntity);
            cashOutRequestEntity.setRrnEntity(rrnEntity);
            cashOutRequestEntity.setAdditionalData(cashOutObjectDTO.getAdditionalData());
            cashOutRequestEntity.setChannel(cashOutObjectDTO.getChannel());
            cashOutRequestEntity.setResult(StatusService.CREATE);
            cashOutRequestEntity.setChannelIp(cashOutObjectDTO.getIp());
            cashOutRequestEntity.setRequestTypeEntity(requestTypeEntity);
            cashOutRequestEntity.setCreatedBy(cashOutObjectDTO.getChannel().getUsername());
            cashOutRequestEntity.setCreatedAt(new Date());
            try {
                requestService.save(cashOutRequestEntity);
            } catch (Exception ex) {
                log.error("error in save cashOut with message ({})", ex.getMessage());
                throw new InternalServiceException("error in save cashIn", StatusService.GENERAL_ERROR, HttpStatus.OK);
            }
            cashOutRequestEntity.setResult(StatusService.SUCCESSFUL);
            cashOutRequestEntity.setAdditionalData(cashOutRequestEntity.getAdditionalData());

            TransactionEntity transaction = new TransactionEntity();
            transaction.setAmount(BigDecimal.valueOf(cashOutRequestEntity.getAmount()));
            transaction.setWalletAccountEntity(walletAccountEntity);
            transaction.setAdditionalData(cashOutRequestEntity.getAdditionalData());
            transaction.setRequestTypeId(cashOutRequestEntity.getRequestTypeEntity().getId());

            Map<String, Object> model = new HashMap<>();
            model.put("amount", Utility.addComma(cashOutRequestEntity.getAmount()));
            model.put("traceId", String.valueOf(rrnEntity.getId()));
            model.put("additionalData", cashOutRequestEntity.getAdditionalData());
            String templateMessage = templateService.getTemplate(TemplateService.CASH_OUT);
            transaction.setDescription(messageResolverService.resolve(templateMessage, model));
            transactionService.insertWithdraw(transaction);
            log.info("balance for walletAccount ===> {} update successful", walletAccountEntity.getAccountNumber());

            requestService.save(cashOutRequestEntity);

            log.info("Start updating CashOutLimitation for walletAccount ({})", walletAccountEntity.getAccountNumber());
            walletCashLimitationService.updateCashOutLimitation(walletAccountEntity, BigDecimal.valueOf(Long.parseLong(cashOutObjectDTO.getAmount())));
            log.info("updating CashOutLimitation for walletAccount ({}) is finished.", walletAccountEntity.getAccountNumber());

            BigDecimal walletAccountServiceBalance = walletAccountService.getBalance(walletAccountEntity.getId());

            return helper.fillCashOutResponse(cashOutObjectDTO.getNationalCode(), rrnEntity.getUuid(), String.valueOf(walletAccountServiceBalance), walletAccountEntity.getAccountNumber());
        }, cashOutObjectDTO.getUniqueIdentifier());
    }

    @Override
    public CashOutTrackResponse inquiry(ChannelEntity channelEntity, String uuid, String channelIp) throws InternalServiceException {
        RequestTypeEntity requestTypeEntity = requestTypeService.getRequestType(RequestTypeService.CASH_OUT);
        RrnEntity rrnEntity = rrnService.findByUid(uuid);
        rrnService.checkRrn(uuid, channelEntity, requestTypeEntity,"","");
        CashOutRequestEntity cashOutRequestEntity = requestService.findCashOutWithRrnId(rrnEntity.getId());
        return helper.fillCashOutTrackResponse(cashOutRequestEntity, statusService);
    }


    @Override
    public UuidResponse physicalGenerateUuid(ChannelEntity channelEntity, String nationalCode, String quantity, String accountNumber) throws InternalServiceException {
        try {
            WalletAccountEntity walletAccountEntity = helper.checkWalletAndWalletAccountForNormalUser(walletService, nationalCode, walletAccountService, accountNumber);
            walletCashLimitationService.checkPhysicalCashOutLimitation(channelEntity, walletAccountEntity.getWalletEntity(), new BigDecimal(quantity), walletAccountEntity);
            log.info("start physicalGenerateUuid traceId, username ===> ({}), nationalCode ({})", channelEntity.getUsername(), nationalCode);
            RrnEntity rrnEntity = rrnService.generateTraceId(nationalCode, channelEntity, requestTypeService.getRequestType(RequestTypeService.PHYSICAL_CASH_OUT), accountNumber, quantity);
            log.info("finish physicalGenerateUuid traceId ===> {}, username ({}), nationalCode ({})", rrnEntity.getUuid(), channelEntity.getUsername(), nationalCode);
            return new UuidResponse(rrnEntity.getUuid());
        } catch (InternalServiceException e) {
            log.error("error in physicalGenerateUuid traceId with info ===> username ({}), nationalCode ({}) error ===> ({})", channelEntity.getUsername(), nationalCode, e.getMessage());
            throw e;
        }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public PhysicalCashOutResponse physicalWithdrawal(PhysicalCashOutObjectDTO physicalCashOutObjectDTO) throws InternalServiceException {

        if (!physicalCashOutObjectDTO.getCurrency().equalsIgnoreCase(physicalCashOutObjectDTO.getCommissionType())) {
            log.error("commission and currency not be same!!!");
            throw new InternalServiceException("commission and currency not be same", StatusService.COMMISSION_CURRENCY_NOT_VALID, HttpStatus.OK);
        }

        RequestTypeEntity requestTypeEntity = requestTypeService.getRequestType(RequestTypeService.PHYSICAL_CASH_OUT);
        WalletAccountEntity channelCommissionAccount = findChannelCommissionAccount(physicalCashOutObjectDTO.getChannel(), physicalCashOutObjectDTO.getCommissionType());


        RrnEntity rrnEntity = rrnService.findByUid(physicalCashOutObjectDTO.getUniqueIdentifier());

        return redisLockService.runAfterLock(physicalCashOutObjectDTO.getAccountNumber(), this.getClass(), () -> {
            log.info("start physicalWithdrawal checking existence of traceId({}) ...", physicalCashOutObjectDTO.getUniqueIdentifier());
            rrnService.checkRrn(physicalCashOutObjectDTO.getUniqueIdentifier(), physicalCashOutObjectDTO.getChannel(), requestTypeEntity, String.valueOf(physicalCashOutObjectDTO.getQuantity()), physicalCashOutObjectDTO.getAccountNumber());
            log.info("finish physicalWithdrawal checking existence of traceId({})", physicalCashOutObjectDTO.getUniqueIdentifier());

            requestService.findPhyicalCashOutDuplicateWithRrnId(rrnEntity.getId());

            WalletAccountEntity walletAccountEntity = helper.checkWalletAndWalletAccountForNormalUser(walletService, rrnEntity.getNationalCode(), walletAccountService, physicalCashOutObjectDTO.getAccountNumber());
            walletCashLimitationService.checkPhysicalCashOutLimitation(physicalCashOutObjectDTO.getChannel(), walletAccountEntity.getWalletEntity(), physicalCashOutObjectDTO.getQuantity(), walletAccountEntity);

            PhysicalCashOutRequestEntity physicalCashOutRequestEntity = new PhysicalCashOutRequestEntity();
            physicalCashOutRequestEntity.setQuantity(physicalCashOutObjectDTO.getQuantity());
            physicalCashOutRequestEntity.setCommission(physicalCashOutObjectDTO.getCommission());
            physicalCashOutRequestEntity.setFinalQuantity(physicalCashOutObjectDTO.getQuantity().subtract(physicalCashOutObjectDTO.getCommission()));
            physicalCashOutRequestEntity.setWalletAccountEntity(walletAccountEntity);
            physicalCashOutRequestEntity.setRrnEntity(rrnEntity);
            physicalCashOutRequestEntity.setAdditionalData(physicalCashOutObjectDTO.getAdditionalData());
            physicalCashOutRequestEntity.setChannel(physicalCashOutObjectDTO.getChannel());
            physicalCashOutRequestEntity.setResult(StatusService.CREATE);
            physicalCashOutRequestEntity.setChannelIp(physicalCashOutObjectDTO.getIp());
            physicalCashOutRequestEntity.setRequestTypeEntity(requestTypeEntity);
            physicalCashOutRequestEntity.setCreatedBy(physicalCashOutObjectDTO.getChannel().getUsername());
            physicalCashOutRequestEntity.setCreatedAt(new Date());
            try {
                requestService.save(physicalCashOutRequestEntity);
            } catch (Exception ex) {
                log.error("error in save physicalCashOutRequestEntity with message ({})", ex.getMessage());
                throw new InternalServiceException("error in save cashIn", StatusService.GENERAL_ERROR, HttpStatus.OK);
            }
            physicalCashOutRequestEntity.setResult(StatusService.SUCCESSFUL);
            physicalCashOutRequestEntity.setAdditionalData(physicalCashOutRequestEntity.getAdditionalData());

            TransactionEntity transaction = new TransactionEntity();
            transaction.setAmount(physicalCashOutRequestEntity.getQuantity());
            transaction.setWalletAccountEntity(walletAccountEntity);
            transaction.setAdditionalData(physicalCashOutRequestEntity.getAdditionalData());
            transaction.setRequestTypeId(physicalCashOutRequestEntity.getRequestTypeEntity().getId());

            Map<String, Object> model = new HashMap<>();
            model.put("amount", Utility.addComma(physicalCashOutRequestEntity.getQuantity().longValue()));
            model.put("traceId", String.valueOf(rrnEntity.getId()));
            model.put("additionalData", physicalCashOutRequestEntity.getAdditionalData());
            String templateMessage = templateService.getTemplate(TemplateService.PHYSICAL_CASH_OUT);
            transaction.setDescription(messageResolverService.resolve(templateMessage, model));
            transactionService.insertWithdraw(transaction);
            log.info("balance for walletAccount ===> {} update successful", walletAccountEntity.getAccountNumber());

            //commission type must be currency
            if (physicalCashOutObjectDTO.getCommission().compareTo(BigDecimal.valueOf(0L)) > 0) {
                log.info("start sell transaction for uniqueIdentifier ({}), commission ({}) for deposit commission from nationalCode ({}), walletAccountId ({})", physicalCashOutRequestEntity.getRrnEntity().getUuid(), physicalCashOutObjectDTO.getCommission(), physicalCashOutObjectDTO.getNationalCode()
                        , channelCommissionAccount.getId());
                TransactionEntity commissionDeposit = createTransaction(channelCommissionAccount, physicalCashOutObjectDTO.getCommission(),
                        messageResolverService.resolve(templateMessage, model), physicalCashOutRequestEntity.getAdditionalData(), physicalCashOutRequestEntity.getRequestTypeEntity().getId(), physicalCashOutRequestEntity.getRrnEntity());
                transactionService.insertDeposit(commissionDeposit);
                log.info("finish sell transaction for uniqueIdentifier ({}), commission ({}) for deposit commission from nationalCode ({}) with transactionId ({})", physicalCashOutRequestEntity.getRrnEntity().getId(), physicalCashOutObjectDTO.getCommission(), physicalCashOutObjectDTO.getNationalCode(), commissionDeposit.getId());
            }

            requestService.save(physicalCashOutRequestEntity);

            log.info("Start updating physicalCashOutLimitation for walletAccount ({})", walletAccountEntity.getAccountNumber());
            walletCashLimitationService.updatePhysicalCashOutLimitation(walletAccountEntity, physicalCashOutRequestEntity.getFinalQuantity());
            log.info("updating physicalCashOutLimitation for walletAccount ({}) is finished.", walletAccountEntity.getAccountNumber());

            BigDecimal walletAccountServiceBalance = walletAccountService.getBalance(walletAccountEntity.getId());

            return helper.fillPhysicalCashOutResponse(physicalCashOutObjectDTO.getNationalCode(), rrnEntity.getUuid(), String.valueOf(walletAccountServiceBalance), walletAccountEntity.getAccountNumber());
        }, physicalCashOutObjectDTO.getUniqueIdentifier());
    }

    @Override
    public PhysicalCashOutTrackResponse physicalInquiry(ChannelEntity channelEntity, String uuid, String channelIp) throws InternalServiceException {
        RequestTypeEntity requestTypeEntity = requestTypeService.getRequestType(RequestTypeService.PHYSICAL_CASH_OUT);
        RrnEntity rrnEntity = rrnService.findByUid(uuid);
        rrnService.checkRrn(uuid, channelEntity, requestTypeEntity,"","");
        PhysicalCashOutRequestEntity requestEntity = requestService.findPhysicalCashOutWithRrnId(rrnEntity.getId());
        return helper.fillPhysicalCashOutTrackResponse(requestEntity, statusService);
    }


    private WalletAccountEntity findChannelCommissionAccount(ChannelEntity channel, String walletAccountTypeName) throws InternalServiceException {

        List<WalletAccountEntity> accounts = walletAccountService.findByWallet(channel.getWalletEntity());
        if (accounts.isEmpty()) {
            log.error("No wallet accounts found for channel {}", channel.getUsername());
            throw new InternalServiceException("na wallet account found for channel", StatusService.CHANNEL_WALLET_ACCOUNT_NOT_FOUND, HttpStatus.OK);
        }

        WalletAccountTypeEntity wageType = walletAccountTypeService.getAll().stream()
                .filter(x -> x.getName().equalsIgnoreCase(WalletAccountTypeService.WAGE)).findFirst()
                .orElseThrow(() -> {
                    log.error("Wallet account type wage not found for channel {}", channel.getUsername());
                    return new InternalServiceException("Wallet account type wage not found", StatusService.WALLET_ACCOUNT_TYPE_NOT_FOUND, HttpStatus.OK);
                });

        return accounts.stream()
                .filter(x -> x.getWalletAccountCurrencyEntity().getName().equalsIgnoreCase(walletAccountTypeName)
                        && x.getWalletAccountTypeEntity().getName().equalsIgnoreCase(wageType.getName())).findFirst().orElseThrow(() -> {
                    log.error("Commission account not found for channel {}", channel.getUsername());
                    return new InternalServiceException("Commission account not found", StatusService.CHANNEL_WALLET_ACCOUNT_NOT_FOUND, HttpStatus.OK);
                });
    }

    private TransactionEntity createTransaction(WalletAccountEntity account, BigDecimal amount, String description,
                                                String additionalData, long requestTypeId,
                                                RrnEntity rrn) {
        TransactionEntity transaction = new TransactionEntity();
        transaction.setAmount(amount);
        transaction.setWalletAccountEntity(account);
        transaction.setDescription(description);
        transaction.setAdditionalData(additionalData);
        transaction.setRequestTypeId(requestTypeId);
        transaction.setRrnEntity(rrn);
        return transaction;
    }
}
