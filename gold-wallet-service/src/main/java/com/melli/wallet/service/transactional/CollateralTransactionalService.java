package com.melli.wallet.service.transactional;

import com.melli.wallet.domain.master.entity.*;
import com.melli.wallet.exception.InternalServiceException;
import com.melli.wallet.service.operation.MessageResolverOperationService;
import com.melli.wallet.service.repository.*;
import com.melli.wallet.util.Utility;
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
    private final RequestTypeRepositoryService requestTypeRepositoryService;
    private final RequestRepositoryService requestRepositoryService;


    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void unblockAndTransfer(CreateCollateralRequestEntity requestEntity) throws InternalServiceException{

        log.info("=== COLLATERAL UNBLOCK AND TRANSFER PROCESS START ===");
        log.info("Input parameters - uniqueIdentifier: {}, quantity: {}, collateralName: {}, userAccountNumber: {}", 
            requestEntity.getRrnEntity().getUuid(), requestEntity.getQuantity(), 
            requestEntity.getCollateralEntity().getName(), requestEntity.getWalletAccountEntity().getAccountNumber());

        WalletAccountEntity userWalletAccountNumber = requestEntity.getWalletAccountEntity();
        log.debug("User wallet account found - accountId: {}, accountNumber: {}, currency: {}", 
            userWalletAccountNumber.getId(), userWalletAccountNumber.getAccountNumber(), 
            userWalletAccountNumber.getWalletAccountCurrencyEntity().getName());

        log.debug("Searching for collateral wallet account with currency: {}", 
            userWalletAccountNumber.getWalletAccountCurrencyEntity().getName());
        Optional<WalletAccountEntity> collateralWalletAccountNumber = walletAccountRepositoryService.findByWallet(requestEntity.getCollateralEntity().getWalletEntity()).stream().filter(x->x.getWalletAccountCurrencyEntity().getId() == userWalletAccountNumber.getWalletAccountCurrencyEntity().getId()).findFirst();
        
        if(collateralWalletAccountNumber.isEmpty()){
            log.error("Collateral wallet account not found - currency: {}, collateralCompany: {}", 
                userWalletAccountNumber.getWalletAccountCurrencyEntity().getName(), requestEntity.getCollateralEntity().getName());
            throw new InternalServiceException("walletAccount not found", StatusRepositoryService.COLLATERAL_ACCOUNT_NOT_FOUND, HttpStatus.OK);
        }
        log.info("Collateral wallet account found - accountId: {}, accountNumber: {}", 
            collateralWalletAccountNumber.get().getId(), collateralWalletAccountNumber.get().getAccountNumber());

        String key = userWalletAccountNumber.getAccountNumber();
        String uniqueIdentifier = requestEntity.getRrnEntity().getUuid();
        log.info("Starting Redis lock acquisition for account: {}", key);
        
        redisLockService.runAfterLock(key, this.getClass(), () -> {
            log.info("=== LOCK ACQUIRED - STARTING UNBLOCK AND TRANSFER CRITICAL SECTION ===");
            log.info("Processing unblock and transfer transactions for uniqueIdentifier: {}", requestEntity.getRrnEntity().getUuid());
            
            log.debug("Retrieving transaction templates");
            String depositTemplate = templateRepositoryService.getTemplate(TemplateRepositoryService.COLLATERAL_SEIZE_DEPOSIT);
            String withdrawalTemplate = templateRepositoryService.getTemplate(TemplateRepositoryService.COLLATERAL_SEIZE_WITHDRAWAL);
            log.debug("Templates retrieved - deposit: {}, withdrawal: {}", depositTemplate, withdrawalTemplate);

            log.debug("Preparing transaction model");
            Map<String, Object> model = new HashMap<>();
            model.put("traceId", String.valueOf(requestEntity.getRrnEntity().getId()));
            model.put("srcAccountNumber", userWalletAccountNumber.getAccountNumber());
            model.put("dstCompany", requestEntity.getCollateralEntity().getName());
            model.put("amount", requestEntity.getQuantity());
            log.debug("Transaction model prepared - traceId: {}, srcAccount: {}, dstCompany: {}, amount: {}", 
                model.get("traceId"), model.get("srcAccountNumber"), model.get("dstCompany"), model.get("amount"));

            log.info("=== CREATING USER WITHDRAWAL TRANSACTION ===");
            log.info("Creating user withdrawal transaction - uniqueIdentifier: {}, quantity: {}, walletAccountId: {}", 
                requestEntity.getRrnEntity().getUuid(), requestEntity.getQuantity(), userWalletAccountNumber.getId());

            TransactionEntity userFirstWithdrawal = createTransaction(
                    userWalletAccountNumber, requestEntity.getQuantity(),
                    messageResolverOperationService.resolve(depositTemplate, model), requestEntity.getAdditionalData(), requestEntity, requestEntity.getRrnEntity());
            log.info("User withdrawal transaction created - transactionId: {}, amount: {}", 
                userFirstWithdrawal.getId(), userFirstWithdrawal.getAmount());

            log.info("=== CREATING COLLATERAL DEPOSIT TRANSACTION ===");
            log.info("Creating collateral deposit transaction - uniqueIdentifier: {}, quantity: {}, walletAccountId: {}", 
                requestEntity.getRrnEntity().getUuid(), requestEntity.getQuantity(), collateralWalletAccountNumber.get().getId());

            TransactionEntity userSecondDeposit = createTransaction(collateralWalletAccountNumber.get(),requestEntity.getQuantity(),
                    messageResolverOperationService.resolve(withdrawalTemplate, model), requestEntity.getAdditionalData(), requestEntity, requestEntity.getRrnEntity());
            log.info("Collateral deposit transaction created - transactionId: {}, amount: {}", 
                userSecondDeposit.getId(), userSecondDeposit.getAmount());

            log.info("=== EXECUTING TRANSFER OPERATION ===");
            log.debug("Executing transferBlockWithdrawAndTransfer - userWithdrawalId: {}, collateralDepositId: {}", 
                userFirstWithdrawal.getId(), userSecondDeposit.getId());
            transactionRepositoryService.transferBlockWithdrawAndTransfer(userFirstWithdrawal, userSecondDeposit);
            log.info("Transfer operation completed successfully");
            
            log.info("=== COLLATERAL UNBLOCK AND TRANSFER PROCESS COMPLETED SUCCESSFULLY ===");
            return null;
        }, uniqueIdentifier);
    }

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void purchaseAndCharge(SellCollateralRequestEntity sellCollateralRequestEntity) throws InternalServiceException{

        log.info("=== COLLATERAL PURCHASE AND CHARGE PROCESS START ===");
        log.info("Input parameters - uniqueIdentifier: {}, quantity: {}, price: {}, commission: {}, merchantId: {}", 
            sellCollateralRequestEntity.getRrnEntity().getUuid(), sellCollateralRequestEntity.getQuantity(), 
            sellCollateralRequestEntity.getPrice(), sellCollateralRequestEntity.getCommission(), 
            sellCollateralRequestEntity.getMerchantEntity().getId());

        log.debug("Retrieving transaction templates");
        String depositUserTemplate = templateRepositoryService.getTemplate(TemplateRepositoryService.COLLATERAL_RETURN_AFTER_SELL_DEPOSIT);
        String depositTemplate = templateRepositoryService.getTemplate(TemplateRepositoryService.COLLATERAL_SELL_DEPOSIT);
        String withdrawalTemplate = templateRepositoryService.getTemplate(TemplateRepositoryService.COLLATERAL_SELL_WITHDRAWAL);
        log.debug("Templates retrieved - depositUser: {}, deposit: {}, withdrawal: {}", 
            depositUserTemplate, depositTemplate, withdrawalTemplate);

        log.debug("Preparing transaction model");
        Map<String, Object> model = new HashMap<>();
        model.put("traceId", String.valueOf(sellCollateralRequestEntity.getRrnEntity().getId()));
        model.put("additionalData", sellCollateralRequestEntity.getAdditionalData());
        model.put("amount", sellCollateralRequestEntity.getQuantity());
        model.put("price", sellCollateralRequestEntity.getPrice());
        model.put("merchant", sellCollateralRequestEntity.getMerchantEntity().getName());
        log.debug("Transaction model prepared - traceId: {}, amount: {}, price: {}, merchant: {}", 
            model.get("traceId"), model.get("amount"), model.get("price"), model.get("merchant"));

        log.info("=== MERCHANT VALIDATION ===");
        if(sellCollateralRequestEntity.getMerchantEntity().getStatus() == MerchantRepositoryService.DISABLED){
            log.error("Merchant status validation failed - merchant is disabled - merchantId: {}, status: {}", 
                sellCollateralRequestEntity.getMerchantEntity().getId(), sellCollateralRequestEntity.getMerchantEntity().getStatus());
            throw new InternalServiceException("merchant is disable", StatusRepositoryService.MERCHANT_IS_DISABLE, HttpStatus.OK);
        }
        log.info("Merchant status validation passed - merchant is active");

        MerchantEntity merchant = sellCollateralRequestEntity.getMerchantEntity();
        log.debug("Merchant entity retrieved - id: {}, name: {}, status: {}", 
            merchant.getId(), merchant.getName(), merchant.getStatus());

        log.info("=== CURRENCY VALIDATION ===");
        WalletAccountCurrencyEntity currencyEntity = sellCollateralRequestEntity.getCreateCollateralRequestEntity().getWalletAccountEntity().getWalletAccountCurrencyEntity();
        WalletAccountCurrencyEntity rialCurrencyEntity = walletAccountCurrencyRepositoryService.findCurrency(WalletAccountCurrencyRepositoryService.RIAL);
        log.info("Currency entities found - currency: {}, rial: {}", 
            currencyEntity.getName(), rialCurrencyEntity.getName());

        log.info("=== MERCHANT ACCOUNT VALIDATION ===");
        WalletAccountEntity merchantCurrencyAccount = merchantRepositoryService.findMerchantWalletAccount(merchant, currencyEntity);
        WalletAccountEntity merchantRialAccount = merchantRepositoryService.findMerchantWalletAccount(merchant, rialCurrencyEntity);
        log.info("Merchant accounts found - currencyAccount: {}, rialAccount: {}", 
            merchantCurrencyAccount.getId(), merchantRialAccount.getId());

        log.info("=== COLLATERAL ACCOUNT VALIDATION ===");
        WalletEntity collateralWallet = sellCollateralRequestEntity.getCreateCollateralRequestEntity().getCollateralEntity().getWalletEntity();
        WalletAccountEntity collateralRialAccount = walletAccountRepositoryService.findUserWalletAccount(collateralWallet, rialCurrencyEntity, WalletAccountCurrencyRepositoryService.RIAL);
        WalletAccountEntity collateralCurrencyAccount = sellCollateralRequestEntity.getCollateralWalletAccountEntity();
        log.info("Collateral accounts found - rialAccount: {}, currencyAccount: {}", 
            collateralRialAccount.getId(), collateralCurrencyAccount.getId());

        log.info("=== CHANNEL COMMISSION VALIDATION ===");
        WalletAccountEntity channelCommissionAccount = walletAccountRepositoryService.findChannelCommissionAccount(sellCollateralRequestEntity.getChannel(),
                collateralCurrencyAccount.getWalletAccountCurrencyEntity().getName());
        log.info("Channel commission account found - accountId: {}, accountNumber: {}, currency: {}", 
            channelCommissionAccount.getId(), channelCommissionAccount.getAccountNumber(), 
            collateralCurrencyAccount.getWalletAccountCurrencyEntity().getName());


        log.info("=== PROCESSING COLLATERAL CURRENCY WITHDRAWAL ===");
        BigDecimal totalWithdrawalAmount = sellCollateralRequestEntity.getQuantity().add(sellCollateralRequestEntity.getCommission());
        log.info("Creating collateral currency withdrawal - uniqueIdentifier: {}, quantity: {}, commission: {}, totalAmount: {}, walletAccountId: {}", 
            sellCollateralRequestEntity.getRrnEntity().getUuid(), sellCollateralRequestEntity.getQuantity(), 
            sellCollateralRequestEntity.getCommission(), totalWithdrawalAmount, collateralCurrencyAccount.getId());
        
        TransactionEntity collateralCurrencyWithdrawal = createTransaction(collateralCurrencyAccount,
                totalWithdrawalAmount,
                messageResolverOperationService.resolve(withdrawalTemplate, model), sellCollateralRequestEntity.getAdditionalData(),
                sellCollateralRequestEntity, sellCollateralRequestEntity.getRrnEntity());
        
        log.debug("Executing collateral currency withdrawal transaction - transactionId: {}, amount: {}", 
            collateralCurrencyWithdrawal.getId(), collateralCurrencyWithdrawal.getAmount());
        transactionRepositoryService.insertWithdraw(collateralCurrencyWithdrawal);
        log.info("Collateral currency withdrawal completed successfully - transactionId: {}", collateralCurrencyWithdrawal.getId());

        log.info("=== PROCESSING MERCHANT RIAL WITHDRAWAL ===");
        log.info("Creating merchant rial withdrawal - uniqueIdentifier: {}, price: {}, merchantId: {}, walletAccountId: {}", 
            sellCollateralRequestEntity.getRrnEntity().getUuid(), sellCollateralRequestEntity.getPrice(),
            sellCollateralRequestEntity.getMerchantEntity().getId(), merchantRialAccount.getId());

        TransactionEntity merchantRialWithdrawal = createTransaction(
                merchantRialAccount, BigDecimal.valueOf(sellCollateralRequestEntity.getPrice()),
                messageResolverOperationService.resolve(withdrawalTemplate, model), sellCollateralRequestEntity.getAdditionalData(), sellCollateralRequestEntity, sellCollateralRequestEntity.getRrnEntity());
        
        log.debug("Executing merchant rial withdrawal transaction - transactionId: {}, amount: {}", 
            merchantRialWithdrawal.getId(), merchantRialWithdrawal.getAmount());
        transactionRepositoryService.insertWithdraw(merchantRialWithdrawal);
        log.info("Merchant rial withdrawal completed successfully - transactionId: {}, price: {}, merchantId: {}, walletAccountId: {}", 
            merchantRialWithdrawal.getId(), sellCollateralRequestEntity.getPrice(), 
            sellCollateralRequestEntity.getMerchantEntity().getId(), merchantRialAccount.getId());

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

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void cashout(SellCollateralRequestEntity sellCollateralRequestEntity) throws InternalServiceException{
        RequestTypeEntity requestTypeEntity = requestTypeRepositoryService.getRequestType(RequestTypeRepositoryService.CASH_OUT);
        WalletAccountCurrencyEntity rialCurrencyEntity = walletAccountCurrencyRepositoryService.findCurrency(WalletAccountCurrencyRepositoryService.RIAL);
        WalletEntity walletEntity = sellCollateralRequestEntity.getCreateCollateralRequestEntity().getCollateralEntity().getWalletEntity();
        WalletAccountEntity collateralRialAccount = walletAccountRepositoryService.findUserWalletAccount(walletEntity, rialCurrencyEntity, WalletAccountCurrencyRepositoryService.RIAL);
        CashOutRequestEntity cashOutRequestEntity = new CashOutRequestEntity();
        cashOutRequestEntity.setAmount(sellCollateralRequestEntity.getPrice());
        cashOutRequestEntity.setIban(sellCollateralRequestEntity.getIban());
        cashOutRequestEntity.setWalletAccountEntity(collateralRialAccount);
        cashOutRequestEntity.setRrnEntity(sellCollateralRequestEntity.getRrnEntity());
        cashOutRequestEntity.setAdditionalData(sellCollateralRequestEntity.getAdditionalData());
        cashOutRequestEntity.setChannel(sellCollateralRequestEntity.getChannel());
        cashOutRequestEntity.setResult(StatusRepositoryService.CREATE);
        cashOutRequestEntity.setChannelIp(sellCollateralRequestEntity.getChannelIp());
        cashOutRequestEntity.setRequestTypeEntity(requestTypeEntity);
        cashOutRequestEntity.setCreatedBy(sellCollateralRequestEntity.getChannel().getUsername());
        cashOutRequestEntity.setCreatedAt(new Date());
        try {
            requestRepositoryService.save(cashOutRequestEntity);
        } catch (Exception ex) {
            log.error("error in save cashOut with message ({})", ex.getMessage());
            throw new InternalServiceException("error in save cashIn", StatusRepositoryService.GENERAL_ERROR, HttpStatus.OK);
        }
        cashOutRequestEntity.setResult(StatusRepositoryService.SUCCESSFUL);
        cashOutRequestEntity.setAdditionalData(cashOutRequestEntity.getAdditionalData());

        TransactionEntity transaction = new TransactionEntity();
        transaction.setAmount(BigDecimal.valueOf(cashOutRequestEntity.getAmount()));
        transaction.setWalletAccountEntity(collateralRialAccount);
        transaction.setAdditionalData(cashOutRequestEntity.getAdditionalData());
        transaction.setRequestTypeId(cashOutRequestEntity.getRequestTypeEntity().getId());

        Map<String, Object> model = new HashMap<>();
        model.put("amount", Utility.addComma(cashOutRequestEntity.getAmount()));
        model.put("traceId", String.valueOf(cashOutRequestEntity.getRrnEntity().getId()));
        model.put("additionalData", cashOutRequestEntity.getAdditionalData());
        String templateMessage = templateRepositoryService.getTemplate(TemplateRepositoryService.CASH_OUT);
        transaction.setDescription(messageResolverOperationService.resolve(templateMessage, model));
        transactionRepositoryService.insertWithdraw(transaction);
        log.info("balance for walletAccount ===> {} update successful", collateralRialAccount.getAccountNumber());
        requestRepositoryService.save(cashOutRequestEntity);
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
