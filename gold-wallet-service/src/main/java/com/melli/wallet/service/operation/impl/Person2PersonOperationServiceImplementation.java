package com.melli.wallet.service.operation.impl;

import com.melli.wallet.domain.dto.P2pObjectDTO;
import com.melli.wallet.domain.master.entity.*;
import com.melli.wallet.domain.response.p2p.P2pTrackResponse;
import com.melli.wallet.domain.response.p2p.P2pUuidResponse;
import com.melli.wallet.exception.InternalServiceException;
import com.melli.wallet.service.operation.MessageResolverOperationService;
import com.melli.wallet.service.operation.Person2PersonOperationService;
import com.melli.wallet.service.operation.WalletP2pLimitationOperationService;
import com.melli.wallet.service.repository.*;
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
 * Class Name: CashServiceImplementation
 * Author: Mahdi Shirinabadi
 * Date: 1/21/2025
 */
@Service
@Log4j2
@RequiredArgsConstructor
public class Person2PersonOperationServiceImplementation implements Person2PersonOperationService {

    private final RedisLockService redisLockService;
    private final RrnRepositoryService rrnRepositoryService;
    private final RequestRepositoryService requestRepositoryService;
    private final Helper helper;
    private final WalletRepositoryService walletRepositoryService;
    private final WalletAccountRepositoryService walletAccountRepositoryService;
    private final WalletP2pLimitationOperationService walletP2pLimitationOperationService;
    private final RequestTypeRepositoryService requestTypeRepositoryService;
    private final TemplateRepositoryService templateRepositoryService;
    private final TransactionRepositoryService transactionRepositoryService;
    private final MessageResolverOperationService messageResolverOperationService;
    private final StatusRepositoryService statusRepositoryService;
    private final WalletAccountTypeRepositoryService walletAccountTypeRepositoryService;

    @Override
    public P2pUuidResponse generateUuid(ChannelEntity channelEntity, String nationalCode, String amount, String accountNumber, String destAccountNumber) throws InternalServiceException {
        try {

            if(accountNumber.equalsIgnoreCase(destAccountNumber)){
                log.error("src({}) and dst ({}) are same", accountNumber, destAccountNumber);
                throw new InternalServiceException("src and dst account are same in generate uuid", StatusRepositoryService.SRC_ACCOUNT_SAME_DST_ACCOUNT_NUMBER, HttpStatus.OK);
            }

            WalletAccountEntity destinationWalletAccount = walletAccountRepositoryService.findByAccountNumber(destAccountNumber);
            if(destinationWalletAccount == null) {
                log.error("wallet account not found for number ({})", destAccountNumber);
                throw new InternalServiceException("wallet account with number ({}) not exist", StatusRepositoryService.DST_ACCOUNT_NUMBER_NOT_FOUND, HttpStatus.OK);
            }

            WalletAccountEntity walletAccountEntity = helper.checkWalletAndWalletAccountForNormalUser(walletRepositoryService, nationalCode, walletAccountRepositoryService, accountNumber);
            walletP2pLimitationOperationService.checkGeneral(channelEntity, walletAccountEntity.getWalletEntity(), new BigDecimal(amount), walletAccountEntity);
            log.info("start generate traceId, username ===> ({}), nationalCode ({})", channelEntity.getUsername(), nationalCode);
            RrnEntity rrnEntity = rrnRepositoryService.generateTraceId(nationalCode, channelEntity, requestTypeRepositoryService.getRequestType(RequestTypeRepositoryService.P2P), accountNumber, amount);
            log.info("finish traceId ===> {}, username ({}), nationalCode ({})", rrnEntity.getUuid(), channelEntity.getUsername(), nationalCode);

            return helper.fillP2pUuidResponse(destinationWalletAccount.getWalletEntity().getNationalCode(), rrnEntity.getUuid());
        } catch (InternalServiceException e) {
            log.error("error in generate traceId with info ===> username ({}), nationalCode ({}) error ===> ({})", channelEntity.getUsername(), nationalCode, e.getMessage());
            throw e;
        }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void process(P2pObjectDTO p2pObjectDTO) throws InternalServiceException {

        if(p2pObjectDTO.getAccountNumber().equalsIgnoreCase(p2pObjectDTO.getDestAccountNumber())){
            log.error("src({}) and dst ({}) are same", p2pObjectDTO.getAccountNumber(), p2pObjectDTO.getDestAccountNumber());
            throw new InternalServiceException("src and dst account are same", StatusRepositoryService.SRC_ACCOUNT_SAME_DST_ACCOUNT_NUMBER, HttpStatus.OK);
        }

        if((p2pObjectDTO.getQuantity().subtract(p2pObjectDTO.getCommission())).compareTo(new BigDecimal("0")) <= 0){
            log.error("commission ({}) is bigger than quantity ({})", p2pObjectDTO.getCommission(), p2pObjectDTO.getQuantity());
            throw new InternalServiceException("commission is bigger than quantity", StatusRepositoryService.COMMISSION_BIGGER_THAN_QUANTITY, HttpStatus.OK);
        }

        RequestTypeEntity requestTypeEntity = requestTypeRepositoryService.getRequestType(RequestTypeRepositoryService.P2P);
        RrnEntity rrnEntity = rrnRepositoryService.findByUid(p2pObjectDTO.getUniqueIdentifier());

        redisLockService.runAfterLock(p2pObjectDTO.getAccountNumber(), this.getClass(), () -> {
            log.info("start checking existence of traceId({}) ...", p2pObjectDTO.getUniqueIdentifier());
            rrnRepositoryService.checkRrn(p2pObjectDTO.getUniqueIdentifier(), p2pObjectDTO.getChannel(), requestTypeEntity, String.valueOf(p2pObjectDTO.getQuantity()), p2pObjectDTO.getAccountNumber());
            log.info("finish checking existence of traceId({})", p2pObjectDTO.getUniqueIdentifier());

            requestRepositoryService.findP2pDuplicateWithRrnId(rrnEntity.getId());

            WalletAccountEntity destinationWalletAccountEntity = walletAccountRepositoryService.findByAccountNumber(p2pObjectDTO.getDestAccountNumber());

            if(destinationWalletAccountEntity == null) {
                log.error("wallet account not found for number ({})", p2pObjectDTO.getDestAccountNumber());
                throw new InternalServiceException("wallet account with number ({}) not exist", StatusRepositoryService.DST_ACCOUNT_NUMBER_NOT_FOUND, HttpStatus.OK);
            }

            WalletAccountEntity srcWalletAccountEntity = helper.checkWalletAndWalletAccountForNormalUser(walletRepositoryService, rrnEntity.getNationalCode(), walletAccountRepositoryService, p2pObjectDTO.getAccountNumber());

            if(srcWalletAccountEntity.getWalletAccountCurrencyEntity().getId() != destinationWalletAccountEntity.getWalletAccountCurrencyEntity().getId()){
                log.error("currency src({}) and dst ({}) are not same", p2pObjectDTO.getAccountNumber(), p2pObjectDTO.getDestAccountNumber());
                throw new InternalServiceException("src and dst account are same", StatusRepositoryService.CURRENCY_SRC_ACCOUNT_SAME_DST_ACCOUNT_NUMBER, HttpStatus.OK);
            }

            walletP2pLimitationOperationService.checkDailyLimitation(p2pObjectDTO.getChannel(), srcWalletAccountEntity.getWalletEntity(),
                    p2pObjectDTO.getQuantity(), srcWalletAccountEntity, p2pObjectDTO.getUniqueIdentifier());

            Person2PersonRequestEntity requestEntity = new Person2PersonRequestEntity();
            requestEntity.setAmount(p2pObjectDTO.getQuantity());
            requestEntity.setFinalAmount(requestEntity.getAmount().add(p2pObjectDTO.getCommission()));
            requestEntity.setSourceAccountWalletEntity(srcWalletAccountEntity);
            requestEntity.setRrnEntity(rrnEntity);
            requestEntity.setChannel(p2pObjectDTO.getChannel());
            requestEntity.setResult(StatusRepositoryService.CREATE);
            requestEntity.setChannelIp(p2pObjectDTO.getIp());
            requestEntity.setRequestTypeEntity(requestTypeEntity);
            requestEntity.setCreatedBy(p2pObjectDTO.getChannel().getUsername());
            requestEntity.setCreatedAt(new Date());
            requestEntity.setDestinationAccountWalletEntity(destinationWalletAccountEntity);
            requestEntity.setCommission(p2pObjectDTO.getCommission());
            try {
                requestRepositoryService.save(requestEntity);
            } catch (Exception ex) {
                log.error("error in save cashIn with message ({})", ex.getMessage());
                throw new InternalServiceException("error in save cashIn", StatusRepositoryService.GENERAL_ERROR, HttpStatus.OK);
            }
            requestEntity.setResult(StatusRepositoryService.SUCCESSFUL);
            requestEntity.setAdditionalData(p2pObjectDTO.getAdditionalData());

            log.info("start sellProcessTransactions for uniqueIdentifier ({})", requestEntity.getRrnEntity().getUuid());
            String depositTemplate = templateRepositoryService.getTemplate(TemplateRepositoryService.P2P_DEPOSIT);
            String withdrawalTemplate = templateRepositoryService.getTemplate(TemplateRepositoryService.P2P_WITHDRAWAL);

            Map<String, Object> model = new HashMap<>();
            model.put("traceId", String.valueOf(requestEntity.getRrnEntity().getId()));
            model.put("srcAccountNumber", requestEntity.getSourceAccountWalletEntity().getAccountNumber());
            model.put("amount", requestEntity.getAmount());
            model.put("dstAccountNumber", requestEntity.getDestinationAccountWalletEntity().getAccountNumber());
            // user first withdrawal (currency)
            log.info("start p2p transaction for uniqueIdentifier ({}), quantity ({}) for withdrawal walletAccountId ({})", requestEntity.getRrnEntity().getUuid(), requestEntity.getAmount(), srcWalletAccountEntity.getId());

            TransactionEntity userFirstWithdrawal = createTransaction(
                    srcWalletAccountEntity, requestEntity.getAmount().add(requestEntity.getCommission()),
                    messageResolverOperationService.resolve(depositTemplate, model), requestEntity.getAdditionalData(), requestEntity, requestEntity.getRrnEntity());
            transactionRepositoryService.insertWithdraw(userFirstWithdrawal);
            log.info("finish p2p transaction for uniqueIdentifier ({}), quantity ({}) for withdrawal walletAccountId ({})", requestEntity.getRrnEntity().getUuid(), requestEntity.getAmount(), srcWalletAccountEntity.getId());

            if (requestEntity.getCommission().compareTo(BigDecimal.valueOf(0L)) > 0) {
                WalletAccountEntity channelCommissionAccount = findChannelCommissionAccount(p2pObjectDTO.getChannel(), WalletAccountCurrencyRepositoryService.GOLD);
                log.info("start sell transaction for uniqueIdentifier ({}), commission ({}) for deposit commission from nationalCode ({}), walletAccountId ({})", requestEntity.getRrnEntity().getUuid(), requestEntity.getCommission(), requestEntity.getSourceAccountWalletEntity().getWalletEntity().getNationalCode(), channelCommissionAccount.getId());
                TransactionEntity commissionDeposit = createTransaction(channelCommissionAccount, requestEntity.getCommission(),
                        messageResolverOperationService.resolve(depositTemplate, model), requestEntity.getAdditionalData(), requestEntity, requestEntity.getRrnEntity());
                transactionRepositoryService.insertDeposit(commissionDeposit);
                log.info("finish sell transaction for uniqueIdentifier ({}), commission ({}) for deposit commission from nationalCode ({}) with transactionId ({})", requestEntity.getRrnEntity().getId(), requestEntity.getCommission(), requestEntity.getSourceAccountWalletEntity().getWalletEntity().getNationalCode(), commissionDeposit.getId());
            }

            // user second deposit (currency)
            log.info("start p2p transaction for uniqueIdentifier ({}), price ({}) for user deposit currency user walletAccountId({})", requestEntity.getRrnEntity().getUuid(), requestEntity.getAmount(), requestEntity.getDestinationAccountWalletEntity().getId());
            TransactionEntity userSecondDeposit = createTransaction(destinationWalletAccountEntity,requestEntity.getAmount(),
                    messageResolverOperationService.resolve(withdrawalTemplate, model), requestEntity.getAdditionalData(), requestEntity, requestEntity.getRrnEntity());
            transactionRepositoryService.insertDeposit(userSecondDeposit);
            log.info("finish p2p transaction for uniqueIdentifier ({}), price ({}) for user deposit currency user walletAccountId({})", requestEntity.getRrnEntity().getUuid(), requestEntity.getAmount(), requestEntity.getDestinationAccountWalletEntity().getId());

            requestRepositoryService.save(requestEntity);

            log.info("Start updating CashInLimitation for walletAccount ({})", srcWalletAccountEntity.getAccountNumber());
            walletP2pLimitationOperationService.updateLimitation(srcWalletAccountEntity, p2pObjectDTO.getQuantity(), p2pObjectDTO.getUniqueIdentifier());
            log.info("updating CashInLimitation for walletAccount ({}) is finished.", srcWalletAccountEntity.getAccountNumber());
            return null;
        }, p2pObjectDTO.getUniqueIdentifier());
    }


    @Override
    public P2pTrackResponse inquiry(ChannelEntity channelEntity, String uuid, String channelIp) throws InternalServiceException {
        RequestTypeEntity requestTypeEntity = requestTypeRepositoryService.getRequestType(RequestTypeRepositoryService.P2P);
        RrnEntity rrnEntity = rrnRepositoryService.findByUid(uuid);
        rrnRepositoryService.checkRrn(uuid, channelEntity, requestTypeEntity, "", "");
        Person2PersonRequestEntity entity = requestRepositoryService.findP2pWithRrnId(rrnEntity.getId());
        return helper.fillP2pTrackResponse(entity, statusRepositoryService);
    }


    private TransactionEntity createTransaction(WalletAccountEntity account, BigDecimal amount, String description,
                                                String additionalData, RequestEntity requestEntity,
                                                RrnEntity rrn) {
        TransactionEntity transaction = new TransactionEntity();
        transaction.setAmount(amount);
        transaction.setWalletAccountEntity(account);
        transaction.setDescription(description);
        transaction.setAdditionalData(additionalData);
        transaction.setRequestTypeId(requestEntity.getRequestTypeEntity().getId());
        transaction.setRrnEntity(rrn);
        return transaction;
    }

    private WalletAccountEntity findChannelCommissionAccount(ChannelEntity channel, String walletAccountTypeName) throws InternalServiceException {
        List<WalletAccountEntity> accounts = walletAccountRepositoryService.findByWallet(channel.getWalletEntity());
        if (accounts.isEmpty()) {
            log.error("No wallet accounts found for channel {}", channel.getUsername());
            throw new InternalServiceException("na wallet account found for channel", StatusRepositoryService.CHANNEL_WALLET_ACCOUNT_NOT_FOUND, HttpStatus.OK);
        }

        WalletAccountTypeEntity wageType = walletAccountTypeRepositoryService.findByNameManaged(WalletAccountTypeRepositoryService.WAGE);
        if (wageType == null) {
            log.error("Wallet account type wage not found for channel {}", channel.getUsername());
            throw new InternalServiceException("Wallet account type wage not found", StatusRepositoryService.WALLET_ACCOUNT_TYPE_NOT_FOUND, HttpStatus.OK);
        }

        return accounts.stream()
                .filter(x -> x.getWalletAccountCurrencyEntity().getName().equalsIgnoreCase(walletAccountTypeName)
                        && x.getWalletAccountTypeEntity().getName().equalsIgnoreCase(wageType.getName())).findFirst().orElseThrow(() -> {
                    log.error("Commission account not found for channel {}", channel.getUsername());
                    return new InternalServiceException("Commission account not found", StatusRepositoryService.CHANNEL_WALLET_ACCOUNT_NOT_FOUND, HttpStatus.OK);
                });
    }
}
