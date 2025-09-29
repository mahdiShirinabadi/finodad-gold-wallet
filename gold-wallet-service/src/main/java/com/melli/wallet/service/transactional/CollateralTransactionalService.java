package com.melli.wallet.service.transactional;

import com.melli.wallet.domain.master.entity.*;
import com.melli.wallet.exception.InternalServiceException;
import com.melli.wallet.service.operation.MessageResolverOperationService;
import com.melli.wallet.service.repository.*;
import com.melli.wallet.utils.RedisLockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Class Name: LiquidTransactionalService
 * Author: Mahdi Shirinabadi
 * Date: 9/29/2025
 */
@Service
@Log4j2
@RequiredArgsConstructor
public class CollateralTransactionalService {

    private final RedisLockService redisLockService;
    private final TemplateRepositoryService templateRepositoryService;
    private final WalletAccountRepositoryService walletAccountRepositoryService;
    private final MessageResolverOperationService messageResolverOperationService;
    private final TransactionRepositoryService transactionRepositoryService;


    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void unblockAndTransfer(CreateCollateralRequestEntity requestEntity) throws InternalServiceException{

        WalletAccountEntity userWalletAccountNumber = requestEntity.getWalletAccountEntity();
        Optional<WalletAccountEntity> collateralWalletAccountNumber = walletAccountRepositoryService.findByWallet(requestEntity.getCollateralEntity().getWalletEntity()).stream().filter(x->x.getWalletAccountCurrencyEntity().getId() == userWalletAccountNumber.getWalletAccountCurrencyEntity().getId()).findFirst();
        if(collateralWalletAccountNumber.isEmpty()){
            log.error("walletAccount for currency ({}) not found for collateral company ({})", userWalletAccountNumber.getWalletAccountCurrencyEntity().getName(), requestEntity.getCollateralEntity().getName());
            throw new InternalServiceException("walletAccount not found", StatusRepositoryService.COLLATERAL_ACCOUNT_NOT_FOUND, HttpStatus.OK);
        }

        String key = userWalletAccountNumber.getAccountNumber();
        String uniqueIdentifier = requestEntity.getRrnEntity().getUuid();
        redisLockService.runAfterLock(key, this.getClass(), () -> {

            log.info("start unblockAndTransferProcessTransactions for uniqueIdentifier ({})", requestEntity.getRrnEntity().getUuid());
            String depositTemplate = templateRepositoryService.getTemplate(TemplateRepositoryService.COLLATERAL_SEIZE_DEPOSIT);
            String withdrawalTemplate = templateRepositoryService.getTemplate(TemplateRepositoryService.COLLATERAL_SEIZE_WITHDRAWAL);

            Map<String, Object> model = new HashMap<>();
            model.put("traceId", String.valueOf(requestEntity.getRrnEntity().getId()));
            model.put("srcAccountNumber", userWalletAccountNumber.getAccountNumber());
            model.put("dstCompany", requestEntity.getCollateralEntity().getName());
            model.put("amount", requestEntity.getQuantity());

            // user first withdrawal (currency)
            log.info("start seize transaction for uniqueIdentifier ({}), quantity ({}) for withdrawal walletAccountId ({})", requestEntity.getRrnEntity().getUuid(), requestEntity.getQuantity(), userWalletAccountNumber.getId());

            TransactionEntity userFirstWithdrawal = createTransaction(
                    userWalletAccountNumber, requestEntity.getQuantity(),
                    messageResolverOperationService.resolve(depositTemplate, model), requestEntity.getAdditionalData(), requestEntity, requestEntity.getRrnEntity());


            log.info("start transaction for uniqueIdentifier ({}), price ({}) for user deposit currency user walletAccountId({})", requestEntity.getRrnEntity().getUuid(), requestEntity.getQuantity(), collateralWalletAccountNumber.get().getId());

            TransactionEntity userSecondDeposit = createTransaction(collateralWalletAccountNumber.get(),requestEntity.getQuantity(),
                    messageResolverOperationService.resolve(withdrawalTemplate, model), requestEntity.getAdditionalData(), requestEntity, requestEntity.getRrnEntity());

            transactionRepositoryService.transferBlockWithdrawAndTransfer(userFirstWithdrawal, userSecondDeposit);
            return null;
        }, uniqueIdentifier);
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
}
