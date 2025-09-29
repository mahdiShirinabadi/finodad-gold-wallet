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
    private final MerchantRepositoryService merchantRepositoryService;
    private final WalletAccountCurrencyRepositoryService walletAccountCurrencyRepositoryService;


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

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void purchaseAndCharge(SellCollateralRequestEntity sellCollateralRequestEntity) throws InternalServiceException{


        log.info("start sellCollateralRequestEntity for uniqueIdentifier ({})", sellCollateralRequestEntity.getRrnEntity().getUuid());
        String depositUserTemplate = templateRepositoryService.getTemplate(TemplateRepositoryService.COLLATERAL_RETURN_AFTER_SELL_DEPOSIT);
        String depositTemplate = templateRepositoryService.getTemplate(TemplateRepositoryService.COLLATERAL_SELL_DEPOSIT);
        String withdrawalTemplate = templateRepositoryService.getTemplate(TemplateRepositoryService.COLLATERAL_SELL_WITHDRAWAL);

        Map<String, Object> model = new HashMap<>();
        model.put("traceId", String.valueOf(sellCollateralRequestEntity.getRrnEntity().getId()));
        model.put("additionalData", sellCollateralRequestEntity.getAdditionalData());
        model.put("amount", sellCollateralRequestEntity.getQuantity());
        model.put("price", sellCollateralRequestEntity.getPrice());
        model.put("merchant", sellCollateralRequestEntity.getMerchantEntity().getName());

        if(sellCollateralRequestEntity.getMerchantEntity().getStatus() == MerchantRepositoryService.DISABLED){
            log.error("merchant is disable and system can not buy any things");
            throw new InternalServiceException("merchant is disable", StatusRepositoryService.MERCHANT_IS_DISABLE, HttpStatus.OK);
        }

        MerchantEntity merchant = sellCollateralRequestEntity.getMerchantEntity();

        WalletAccountCurrencyEntity currencyEntity = sellCollateralRequestEntity.getCreateCollateralRequestEntity().getWalletAccountEntity().getWalletAccountCurrencyEntity();
        WalletAccountCurrencyEntity rialCurrencyEntity = walletAccountCurrencyRepositoryService.findCurrency(WalletAccountCurrencyRepositoryService.RIAL);

        WalletAccountEntity merchantCurrencyAccount = merchantRepositoryService.findMerchantWalletAccount(merchant, currencyEntity);
        WalletAccountEntity merchantRialAccount = merchantRepositoryService.findMerchantWalletAccount(merchant, rialCurrencyEntity);

        // Validate user and wallet accounts
        WalletEntity collateralWallet = sellCollateralRequestEntity.getCreateCollateralRequestEntity().getCollateralEntity().getWalletEntity();
        WalletAccountEntity collateralRialAccount = walletAccountRepositoryService.findUserWalletAccount(collateralWallet, rialCurrencyEntity, WalletAccountCurrencyRepositoryService.RIAL);
        WalletAccountEntity collateralCurrencyAccount = sellCollateralRequestEntity.getCollateralWalletAccountEntity();

        // Validate channel commission account
        WalletAccountEntity channelCommissionAccount = walletAccountRepositoryService.findChannelCommissionAccount(sellCollateralRequestEntity.getChannel(),
                collateralCurrencyAccount.getWalletAccountCurrencyEntity().getName());


        //get MerchantId
        // user withdrawal (currency) (quantity)
        log.info("start purchase transaction for uniqueIdentifier ({}), quantity ({}) for user withdrawal user walletAccountId({})", sellCollateralRequestEntity.getRrnEntity().getUuid(), sellCollateralRequestEntity.getQuantity(), sellCollateralRequestEntity.getMerchantEntity().getWalletEntity().getId());
        TransactionEntity collateralCurrencyWithdrawal = createTransaction(sellCollateralRequestEntity.getCollateralWalletAccountEntity(),
                (sellCollateralRequestEntity.getQuantity().add(sellCollateralRequestEntity.getCommission())),
                messageResolverOperationService.resolve(withdrawalTemplate, model), sellCollateralRequestEntity.getAdditionalData(),
                sellCollateralRequestEntity, sellCollateralRequestEntity.getRrnEntity());
        transactionRepositoryService.insertWithdraw(collateralCurrencyWithdrawal);
        log.info("start sell transaction for uniqueIdentifier ({}), quantity ({}) for user withdrawal user walletAccountId({})", sellCollateralRequestEntity.getRrnEntity().getUuid(), sellCollateralRequestEntity.getQuantity(), merchantCurrencyAccount.getId());


        // merchant withdrawal (rial)
        log.info("start sell transaction for uniqueIdentifier ({}), price ({}) for merchant withdrawal from id ({}), walletAccountId ({})", sellCollateralRequestEntity.getRrnEntity().getUuid(), sellCollateralRequestEntity.getPrice(),
                sellCollateralRequestEntity.getMerchantEntity().getId(), collateralRialAccount.getId());

        TransactionEntity merchantRialWithdrawal = createTransaction(
                merchantRialAccount, BigDecimal.valueOf(sellCollateralRequestEntity.getPrice()),
                messageResolverOperationService.resolve(withdrawalTemplate, model), sellCollateralRequestEntity.getAdditionalData(), sellCollateralRequestEntity, sellCollateralRequestEntity.getRrnEntity());
        transactionRepositoryService.insertWithdraw(merchantRialWithdrawal);
        log.info("finish sell transaction for uniqueIdentifier ({}), price ({}) for merchant withdrawal from id ({}), walletAccountId ({}), transactionId ({})", sellCollateralRequestEntity.getRrnEntity().getUuid(),
                sellCollateralRequestEntity.getPrice(), sellCollateralRequestEntity.getMerchantEntity().getId(), collateralRialAccount.getId(),
                merchantRialWithdrawal.getId());

        // Channel commission deposit (if applicable)
        //commission type must be currency
        if (sellCollateralRequestEntity.getCommission().compareTo(BigDecimal.valueOf(0L)) > 0) {
            log.info("start sell transaction for uniqueIdentifier ({}), commission ({}) for deposit commission from nationalCode ({}), walletAccountId ({})", sellCollateralRequestEntity.getRrnEntity().getUuid(), sellCollateralRequestEntity.getCommission(), sellCollateralRequestEntity.getCreateCollateralRequestEntity().getCollateralEntity().getWalletEntity().getNationalCode(), channelCommissionAccount.getId());
            TransactionEntity commissionDeposit = createTransaction(channelCommissionAccount, sellCollateralRequestEntity.getCommission(),
                    messageResolverOperationService.resolve(depositTemplate, model), sellCollateralRequestEntity.getAdditionalData(),
                    sellCollateralRequestEntity, sellCollateralRequestEntity.getRrnEntity());
            transactionRepositoryService.insertDeposit(commissionDeposit);
            log.info("finish sell transaction for uniqueIdentifier ({}), commission ({}) for deposit commission from nationalCode ({}) with transactionId ({})", sellCollateralRequestEntity.getRrnEntity().getId(), sellCollateralRequestEntity.getCommission(),
                    sellCollateralRequestEntity.getCreateCollateralRequestEntity().getCollateralEntity().getWalletEntity().getNationalCode(), commissionDeposit.getId());
        }

        // collateral deposit (rial) (price)
        log.info("start sell transaction for uniqueIdentifier ({}), price ({}) for user deposit currency user walletAccountId({})",
                sellCollateralRequestEntity.getRrnEntity().getUuid(), sellCollateralRequestEntity.getPrice(), collateralRialAccount.getId());
        TransactionEntity collateralRialDepositTransaction = createTransaction(collateralRialAccount, BigDecimal.valueOf(sellCollateralRequestEntity.getPrice()),
                messageResolverOperationService.resolve(depositTemplate, model), sellCollateralRequestEntity.getAdditionalData(), sellCollateralRequestEntity,
                sellCollateralRequestEntity.getRrnEntity());
        transactionRepositoryService.insertDeposit(collateralRialDepositTransaction);
        log.info("finish sell transaction for uniqueIdentifier ({}), price ({}) for user deposit currency user walletAccountId({}), transactionId ({})", sellCollateralRequestEntity.getRrnEntity().getUuid(),
                sellCollateralRequestEntity.getPrice(), collateralCurrencyAccount.getId(), collateralRialDepositTransaction.getId());



        // merchant deposit (currency) (quantity - commission)
        log.info("start sell transaction for uniqueIdentifier ({}), quantity ({}) for merchant deposit currency user walletAccountId({})",
                sellCollateralRequestEntity.getRrnEntity().getUuid(), sellCollateralRequestEntity.getQuantity().subtract(sellCollateralRequestEntity.getCommission()), collateralCurrencyAccount.getId());
        TransactionEntity merchantCurrencyDepositTransaction = createTransaction(merchantCurrencyAccount, sellCollateralRequestEntity.getQuantity().subtract(sellCollateralRequestEntity.getCommission()),
                messageResolverOperationService.resolve(depositTemplate, model), sellCollateralRequestEntity.getAdditionalData(),
                sellCollateralRequestEntity, sellCollateralRequestEntity.getRrnEntity());
        transactionRepositoryService.insertDeposit(merchantCurrencyDepositTransaction);
        log.info("finish sell transaction for uniqueIdentifier ({}), quantity ({}) for merchant deposit currency user walletAccountId({}), transactionId ({})", sellCollateralRequestEntity.getRrnEntity().getUuid(),
                sellCollateralRequestEntity.getQuantity().subtract(sellCollateralRequestEntity.getCommission()), collateralCurrencyAccount.getId(), merchantCurrencyDepositTransaction.getId());


        //deposit userCurrencyAccount
        WalletAccountEntity userWalletAccountEntity = sellCollateralRequestEntity.getCreateCollateralRequestEntity().getWalletAccountEntity();
        BigDecimal finalQuantityForUser = sellCollateralRequestEntity.getCreateCollateralRequestEntity()
                .getFinalBlockQuantity().subtract(sellCollateralRequestEntity.getQuantity().add(sellCollateralRequestEntity.getCommission()));
        TransactionEntity userCurrencyDepositTransaction = createTransaction(userWalletAccountEntity, finalQuantityForUser,
                messageResolverOperationService.resolve(depositUserTemplate, model), sellCollateralRequestEntity.getAdditionalData(),
                sellCollateralRequestEntity, sellCollateralRequestEntity.getRrnEntity());
        transactionRepositoryService.insertDeposit(userCurrencyDepositTransaction);

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
