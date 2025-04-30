package com.melli.wallet.service.impl;

import com.melli.wallet.domain.enumaration.TransactionTypeEnum;
import com.melli.wallet.domain.master.entity.*;
import com.melli.wallet.domain.response.UuidResponse;
import com.melli.wallet.domain.response.purchase.PurchaseResponse;
import com.melli.wallet.domain.response.purchase.PurchaseTrackResponse;
import com.melli.wallet.exception.InternalServiceException;
import com.melli.wallet.service.*;
import com.melli.wallet.utils.Helper;
import com.melli.wallet.utils.RedisLockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

/**
 * Class Name: PurchaseServiceImplementation
 * Author: Mahdi Shirinabadi
 * Date: 1/27/2025
 */
@Service
@Log4j2
@RequiredArgsConstructor
public class PurchaseServiceImplementation implements PurchaseService {

    private final RedisLockService redisLockService;
    private final RrnService rrnService;
    private final RequestService requestService;
    private final SecurityService securityService;
    private final MerchantService merchantService;
    private final Helper helper;
    private final WalletService walletService;
    private final WalletAccountService walletAccountService;
    private final WalletTypeService walletTypeService;
    private final WalletLimitationService walletLimitationService;
    private final TransactionService transactionService;
    private final MessageResolverService messageResolverService;
    private final TemplateService templateService;
    private final StatusService statusService;
    private final WalletAccountTypeService walletAccountTypeService;
    private final WalletAccountCurrencyService walletAccountCurrencyService;
    private final RequestTypeService requestTypeService;

    @Override
    public UuidResponse generateUuid(ChannelEntity channelEntity, String nationalCode, String amount, String accountNumber, String type) throws InternalServiceException {
        try {
            log.info("start generate traceId, username ===> ({}), nationalCode ({})", channelEntity.getUsername(), nationalCode);
            RrnEntity rrnEntity = rrnService.generateTraceId(nationalCode, channelEntity, requestTypeService.getRequestType(type), accountNumber, amount);
            log.info("finish traceId ===> {}, username ({}), nationalCode ({})", rrnEntity.getUuid(), channelEntity.getUsername(), nationalCode);
            return new UuidResponse(rrnEntity.getUuid());
        } catch (InternalServiceException e) {
            log.error("error in generate traceId with info ===> username ({}), nationalCode ({}) error ===> ({})", channelEntity.getUsername(), nationalCode, e.getMessage());
            throw e;
        }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public PurchaseResponse sell(ChannelEntity channel, String uniqueIdentifier, String amount, String price,
                                 String walletAccountNumber, String signData, String dataForCheckInVerify,
                                 String additionalData, String merchantId,
                                 String nationalCode, String commission, String currency, String ip, String commissionCurrency) throws InternalServiceException {
// Validate and retrieve currencies
        WalletAccountCurrencyEntity currencyEntity = walletAccountCurrencyService.findCurrency(currency);
        WalletAccountCurrencyEntity rialCurrencyEntity = walletAccountCurrencyService.findCurrency(WalletAccountCurrencyService.RIAL);


        // Validate merchant and wallet accounts
        MerchantEntity merchant = findMerchant(merchantId);
        WalletAccountEntity merchantCurrencyAccount = findMerchantWalletAccount(merchant, currencyEntity, currency);
        WalletAccountEntity merchantRialAccount = findMerchantWalletAccount(merchant, rialCurrencyEntity, WalletAccountCurrencyService.RIAL);

        // Validate user and wallet accounts
        WalletEntity userWallet = findUserWallet(nationalCode);
        WalletAccountEntity userCurrencyAccount = findUserWalletAccount(userWallet, currencyEntity, currency);
        WalletAccountEntity userRialAccount = findUserRialAccount(userWallet, rialCurrencyEntity, walletAccountNumber, nationalCode);

        // Validate channel commission account
        WalletAccountEntity channelCommissionAccount = findChannelCommissionAccount(channel);

        return redisLockService.runAfterLock(walletAccountNumber, this.getClass(), () -> processSell(
                channel,
                uniqueIdentifier,
                amount,
                price,
                commission,
                additionalData,
                nationalCode,
                signData,
                dataForCheckInVerify,
                userWallet,
                userRialAccount,
                userCurrencyAccount,
                merchant,
                merchantRialAccount,
                merchantCurrencyAccount,
                channelCommissionAccount
        ), uniqueIdentifier);
    }

    @Override
    public PurchaseTrackResponse purchaseTrack(ChannelEntity channel, String uid, String channelIp, String type) throws InternalServiceException {
        RrnEntity rrnEntity = rrnService.checkRrn(uid, channel, requestTypeService.getRequestType(type),"","");
        PurchaseRequestEntity purchaseRequestEntity = requestService.findPurchaseRequestByRrnId(rrnEntity.getId());
        return helper.fillPurchaseTrackResponse(purchaseRequestEntity, statusService);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public PurchaseResponse buy(ChannelEntity channel, String uniqueIdentifier, String amount, String price, String walletAccountNumber,
                                String signData, String dataForCheckInVerify, String additionalData, String merchantId, String nationalCode, String commission, String currency, String ip, String commissionCurrency) throws InternalServiceException {
        // Validate and retrieve currencies
        WalletAccountCurrencyEntity currencyEntity = walletAccountCurrencyService.findCurrency(currency);
        WalletAccountCurrencyEntity rialCurrencyEntity = walletAccountCurrencyService.findCurrency(WalletAccountCurrencyService.RIAL);


        // Validate merchant and wallet accounts
        MerchantEntity merchant = findMerchant(merchantId);
        WalletAccountEntity merchantCurrencyAccount = findMerchantWalletAccount(merchant, currencyEntity, currency);
        WalletAccountEntity merchantRialAccount = findMerchantWalletAccount(merchant, rialCurrencyEntity, WalletAccountCurrencyService.RIAL);

        // Validate user and wallet accounts
        WalletEntity userWallet = findUserWallet(nationalCode);
        WalletAccountEntity userCurrencyAccount = findUserWalletAccount(userWallet, currencyEntity, currency);
        WalletAccountEntity userRialAccount = findUserRialAccount(userWallet, rialCurrencyEntity, walletAccountNumber, nationalCode);

        // Validate channel commission account
        WalletAccountEntity channelCommissionAccount = findChannelCommissionAccount(channel);

        return redisLockService.runAfterLock(walletAccountNumber, this.getClass(), () -> processBuy(
                channel,
                uniqueIdentifier,
                amount,
                price,
                commission,
                additionalData,
                nationalCode,
                signData,
                dataForCheckInVerify,
                userWallet,
                userRialAccount,
                userCurrencyAccount,
                merchant,
                merchantRialAccount,
                merchantCurrencyAccount,
                channelCommissionAccount
        ), uniqueIdentifier);
    }


    private MerchantEntity findMerchant(String merchantId) throws InternalServiceException {
        MerchantEntity merchant = merchantService.findById(Integer.parseInt(merchantId));
        if (merchant == null) {
            log.error("Merchant ID {} doesn't exist", merchantId);
            throw new InternalServiceException(
                    "Merchant doesn't exist",
                    StatusService.MERCHANT_IS_NOT_EXIST,
                    HttpStatus.OK
            );
        }
        return merchant;
    }

    private WalletAccountEntity findMerchantWalletAccount(
            MerchantEntity merchant, WalletAccountCurrencyEntity currencyEntity,String currency) throws InternalServiceException {
        List<WalletAccountEntity> accounts = walletAccountService.findByWallet(merchant.getWalletEntity());
        if (CollectionUtils.isEmpty(accounts)) {
            log.error("No wallet accounts found for merchant {}", merchant.getName());
            throw new InternalServiceException(
                    "Merchant wallet account not found",
                    StatusService.MERCHANT_WALLET_ACCOUNT_NOT_FOUND,
                    HttpStatus.OK
            );
        }
        return accounts.stream()
                .filter(x -> x.getWalletAccountCurrencyEntity().getId() == (currencyEntity.getId()))
                .findFirst()
                .orElseThrow(() -> {
                    log.error("Wallet account with currency {} not found for merchant {}", currency, merchant.getName());
                    return new InternalServiceException(
                            "Wallet account not found for merchant",
                            StatusService.MERCHANT_WALLET_ACCOUNT_NOT_FOUND,
                            HttpStatus.OK
                    );
                });
    }

    private WalletAccountEntity findUserWalletAccount(
            WalletEntity walletEntity,
            WalletAccountCurrencyEntity currencyEntity,
            String currency
    ) throws InternalServiceException {

        List<WalletAccountEntity> accounts = walletAccountService.findByWallet(walletEntity);

        if (CollectionUtils.isEmpty(accounts)) {
            log.error("No wallet accounts found for nationalCode {}", walletEntity.getNationalCode());
            throw new InternalServiceException("user wallet account not found", StatusService.MERCHANT_WALLET_ACCOUNT_NOT_FOUND, HttpStatus.OK);
        }

        return accounts.stream().filter(x -> x.getWalletAccountCurrencyEntity().getId() == (currencyEntity.getId())).findFirst().orElseThrow(() -> {
            log.error("Wallet account with currency {} not found for user nationalCode {}", currency, walletEntity.getNationalCode());
            return new InternalServiceException("Wallet account not found for nationalCode", StatusService.MERCHANT_WALLET_ACCOUNT_NOT_FOUND, HttpStatus.OK);
        });
    }

    private WalletEntity findUserWallet(String nationalCode) throws InternalServiceException {
        WalletTypeEntity walletType = walletTypeService.getAll().stream()
                .filter(x -> x.getName().equals(WalletTypeService.NORMAL_USER)).findFirst()
                .orElseThrow(() -> {
                    log.error("Wallet type {} not found", WalletTypeService.NORMAL_USER);
                    return new InternalServiceException("Wallet type not found", StatusService.WALLET_TYPE_NOT_FOUND, HttpStatus.OK);
                });

        WalletEntity wallet = walletService.findByNationalCodeAndWalletTypeId(nationalCode, walletType.getId());
        if (wallet == null) {
            log.error("National code {} doesn't exist", nationalCode);
            throw new InternalServiceException("National code doesn't exist", StatusService.NATIONAL_CODE_NOT_FOUND, HttpStatus.OK);
        }
        return wallet;
    }

    private WalletAccountEntity findUserRialAccount(WalletEntity wallet, WalletAccountCurrencyEntity currencyEntity, String walletAccountNumber, String nationalCode) throws InternalServiceException {
        List<WalletAccountEntity> accounts = walletAccountService.findByWallet(wallet);
        if (CollectionUtils.isEmpty(accounts)) {
            log.error("No wallet accounts found for user {}", wallet);
            throw new InternalServiceException("Wallet account not found for user", StatusService.WALLET_ACCOUNT_NOT_FOUND, HttpStatus.OK);
        }
        return accounts.stream().filter(x -> x.getWalletAccountCurrencyEntity().getId() == (currencyEntity.getId())
                && x.getAccountNumber().equalsIgnoreCase(walletAccountNumber)).findFirst().orElseThrow(() -> {
            log.error("Rial wallet account not found for user {}", nationalCode);
            return new InternalServiceException("Wallet account not found for user", StatusService.WALLET_ACCOUNT_NOT_FOUND, HttpStatus.OK);
        });
    }

    private WalletAccountEntity findChannelCommissionAccount(ChannelEntity channel) throws InternalServiceException {
        List<WalletAccountEntity> accounts = walletAccountService.findByWallet(channel.getWalletEntity());
        WalletAccountTypeEntity wageType = walletAccountTypeService.getAll().stream()
                .filter(x -> x.getName().equalsIgnoreCase(WalletAccountTypeService.WAGE)).findFirst()
                .orElseThrow(() -> {
                    log.error("Wallet account type wage not found for channel {}", channel.getUsername());
                    return new InternalServiceException("Wallet account type wage not found", StatusService.WALLET_ACCOUNT_TYPE_NOT_FOUND, HttpStatus.OK);
                });

        return accounts.stream()
                .filter(x -> x.getWalletAccountCurrencyEntity().getName().equalsIgnoreCase(WalletAccountCurrencyService.RIAL)
                        && x.getWalletAccountTypeEntity().getName().equalsIgnoreCase(wageType.getName())).findFirst().orElseThrow(() -> {
                    log.error("Commission account not found for channel {}", channel.getUsername());
                    return new InternalServiceException("Commission account not found", StatusService.CHANNEL_WALLET_ACCOUNT_NOT_FOUND, HttpStatus.OK);
                });
    }

    private PurchaseResponse processBuy(
            ChannelEntity channel,String uniqueIdentifier,
            String amount, String price, String commission, String additionalData,
            String nationalCode, String signData, String dataForCheckInVerify, WalletEntity userWallet,
            WalletAccountEntity userRialAccount, WalletAccountEntity userCurrencyAccount,
            MerchantEntity merchant, WalletAccountEntity merchantRialAccount,
            WalletAccountEntity merchantCurrencyAccount, WalletAccountEntity channelCommissionAccount
    ) throws InternalServiceException {
        log.info("Starting purchase for uniqueIdentifier {}, nationalCode {}", uniqueIdentifier, nationalCode);

        // Validate transaction
        RrnEntity rrn = validateTransaction(channel, uniqueIdentifier, signData, dataForCheckInVerify, requestTypeService.getRequestType(RequestTypeService.BUY), price, userRialAccount.getAccountNumber());
        walletLimitationService.checkPurchaseLimitation(channel, userWallet, Long.parseLong(amount), userRialAccount, merchant);

        // Create purchase request
        PurchaseRequestEntity purchaseRequest = createPurchaseRequest(
                channel, rrn, merchant, userRialAccount, amount,price, commission,additionalData, nationalCode, requestTypeService.getRequestType(RequestTypeService.BUY));

        // Process transactions
        buyProcessTransactions(
                purchaseRequest, rrn, userRialAccount, userCurrencyAccount,
                merchantRialAccount, merchantCurrencyAccount, channelCommissionAccount, amount,
                price, commission, additionalData, nationalCode, merchant.getName());

        // Finalize purchase
        purchaseRequest.setResult(StatusService.SUCCESSFUL);
        log.info("Purchase completed successfully for uniqueIdentifier {}", uniqueIdentifier);
        requestService.save(purchaseRequest);
        return helper.fillPurchaseResponse(purchaseRequest);
    }

    private PurchaseResponse processSell(
            ChannelEntity channel,String uniqueIdentifier,
            String amount, String price, String commission, String additionalData,
            String nationalCode, String signData, String dataForCheckInVerify, WalletEntity userWallet,
            WalletAccountEntity userRialAccount, WalletAccountEntity userCurrencyAccount,
            MerchantEntity merchant, WalletAccountEntity merchantRialAccount,
            WalletAccountEntity merchantCurrencyAccount, WalletAccountEntity channelCommissionAccount
    ) throws InternalServiceException {
        log.info("Starting sell for uniqueIdentifier {}, nationalCode {}", uniqueIdentifier, nationalCode);

        // Validate transaction
        RrnEntity rrn = validateTransaction(channel, uniqueIdentifier, signData, dataForCheckInVerify, requestTypeService.getRequestType(RequestTypeService.SELL), price, userRialAccount.getAccountNumber());
        walletLimitationService.checkPurchaseLimitation(channel, userWallet, Long.parseLong(amount), userRialAccount, merchant);

        // Create purchase request
        PurchaseRequestEntity purchaseRequest = createPurchaseRequest(
                channel, rrn, merchant, userRialAccount, amount,price, commission,additionalData, nationalCode, requestTypeService.getRequestType(RequestTypeService.SELL));

        // Process transactions
        sellProcessTransactions(
                purchaseRequest, rrn, userRialAccount, userCurrencyAccount,
                merchantRialAccount, merchantCurrencyAccount, channelCommissionAccount, amount,
                price, commission, additionalData, nationalCode, merchant.getName());

        // Finalize purchase
        purchaseRequest.setResult(StatusService.SUCCESSFUL);
        log.info("sell completed successfully for uniqueIdentifier {}", uniqueIdentifier);
        requestService.save(purchaseRequest);
        return helper.fillPurchaseResponse(purchaseRequest);
    }

    private RrnEntity validateTransaction(
            ChannelEntity channel, String uniqueIdentifier, String signData, String dataForCheckInVerify, RequestTypeEntity requestTypeEntity, String amount, String accountNumber) throws InternalServiceException {
        log.info("Checking uniqueIdentifier {}", uniqueIdentifier);
        RrnEntity rrn = rrnService.checkRrn(uniqueIdentifier, channel, requestTypeEntity,  amount, accountNumber);
        log.info("Checking traceId {}", rrn.getId());
        requestService.checkTraceIdIsUnique(rrn.getId(), new PurchaseRequestEntity());
        securityService.checkSign(channel, signData, dataForCheckInVerify);
        return rrn;
    }

    private PurchaseRequestEntity createPurchaseRequest(
            ChannelEntity channel, RrnEntity rrn, MerchantEntity merchant, WalletAccountEntity userRialAccount,
            String amount, String price, String commission, String additionalData, String nationalCode, RequestTypeEntity requestTypeEntity

    ) throws InternalServiceException {
        PurchaseRequestEntity request = new PurchaseRequestEntity();
        request.setCreatedAt(new Date());
        request.setCreatedBy(channel.getUsername());
        request.setPrice(Long.parseLong(price));
        request.setAmount(new BigDecimal(amount));
        request.setWalletAccount(userRialAccount);
        request.setMerchantEntity(merchant);
        request.setNationalCode(nationalCode);
        request.setRrnEntity(rrn);
        request.setChannel(channel);
        request.setAdditionalData(additionalData);
        request.setRequestTypeEntity(requestTypeEntity);
        request.setCommission(commission);
        requestService.save(request);
        return request;
    }

    private void buyProcessTransactions(
            PurchaseRequestEntity purchaseRequest,
            RrnEntity rrn,
            WalletAccountEntity userRialAccount,
            WalletAccountEntity userCurrencyAccount,
            WalletAccountEntity merchantRialAccount,
            WalletAccountEntity merchantCurrencyAccount,
            WalletAccountEntity channelCommissionAccount,
            String amount, String price, String commission, String additionalData, String nationalCode, String merchantName
    ) throws InternalServiceException {
        String depositTemplate = templateService.getTemplate(TemplateService.PURCHASE_DEPOSIT);
        String withdrawalTemplate = templateService.getTemplate(TemplateService.PURCHASE_WITHDRAWAL);

        Map<String, Object> model = new HashMap<>();
        model.put("traceId", String.valueOf(rrn.getId()));
        model.put("additionalData", additionalData);
        model.put("amount", amount);
        model.put("price", price);
        model.put("merchant", merchantName);
        model.put("nationalCode", nationalCode);

        // User withdrawal (rial)
        TransactionEntity userWithdrawal = createTransaction( userRialAccount, Long.parseLong(price),
                messageResolverService.resolve(withdrawalTemplate, model), additionalData, purchaseRequest, rrn);
        transactionService.insertWithdraw(userWithdrawal);

        // Channel commission deposit (if applicable)
        double commissionValue = Double.parseDouble(commission);
        if (commissionValue > 0) {
            TransactionEntity commissionDeposit = createTransaction( channelCommissionAccount, commissionValue,
                    messageResolverService.resolve(depositTemplate, model), additionalData, purchaseRequest, rrn);
            transactionService.insertDeposit(commissionDeposit);
        }

        // Merchant deposit (rial)
        TransactionEntity merchantDeposit = createTransaction(
                merchantRialAccount,Double.parseDouble(price) - commissionValue,
                messageResolverService.resolve(depositTemplate, model), additionalData, purchaseRequest, rrn);
        transactionService.insertDeposit(merchantDeposit);

        // Merchant withdrawal (currency)
        TransactionEntity merchantWithdrawal = createTransaction(
                merchantCurrencyAccount, Double.parseDouble(amount),
                messageResolverService.resolve(withdrawalTemplate, model),
                additionalData, purchaseRequest, rrn
        );
        transactionService.insertWithdraw(merchantWithdrawal);

        // User deposit (currency)
        TransactionEntity userDeposit = createTransaction(
                userCurrencyAccount,
                Double.parseDouble(amount), messageResolverService.resolve(depositTemplate, model), additionalData, purchaseRequest, rrn
        );
        transactionService.insertDeposit(userDeposit);
    }

    private TransactionEntity createTransaction(WalletAccountEntity account, double amount, String description,
                                                String additionalData, PurchaseRequestEntity purchaseRequest,
                                                RrnEntity rrn) {
        TransactionEntity transaction = new TransactionEntity();
        transaction.setAmount(amount);
        transaction.setWalletAccountEntity(account);
        transaction.setDescription(description);
        transaction.setAdditionalData(additionalData);
        transaction.setRequestTypeId(purchaseRequest.getRequestTypeEntity().getId());
        transaction.setRrnEntity(rrn);
        return transaction;
    }

    private void sellProcessTransactions(
            PurchaseRequestEntity purchaseRequest, RrnEntity rrn, WalletAccountEntity userRialAccount,
            WalletAccountEntity userCurrencyAccount, WalletAccountEntity merchantRialAccount,
            WalletAccountEntity merchantCurrencyAccount, WalletAccountEntity channelCommissionAccount,
            String amount, String price, String commission, String additionalData, String nationalCode,String merchantName
    ) throws InternalServiceException {
        String depositTemplate = templateService.getTemplate(TemplateService.PURCHASE_DEPOSIT);
        String withdrawalTemplate = templateService.getTemplate(TemplateService.PURCHASE_WITHDRAWAL);

        Map<String, Object> model = new HashMap<>();
        model.put("traceId", String.valueOf(rrn.getId()));
        model.put("additionalData", additionalData);
        model.put("amount", amount);
        model.put("price", price);
        model.put("merchant", merchantName);
        model.put("nationalCode", nationalCode);

        // merchant withdrawal (rial)
        TransactionEntity merchantRialWithdrawal = createTransaction(
                merchantRialAccount, Long.parseLong(price),
                messageResolverService.resolve(withdrawalTemplate, model), additionalData, purchaseRequest, rrn);
        transactionService.insertWithdraw(merchantRialWithdrawal);

        // Channel commission deposit (if applicable)
        double commissionValue = Double.parseDouble(commission);
        if (commissionValue > 0) {
            TransactionEntity commissionDeposit = createTransaction(channelCommissionAccount, commissionValue,
                    messageResolverService.resolve(depositTemplate, model), additionalData, purchaseRequest, rrn);
            transactionService.insertDeposit(commissionDeposit);
        }

        // user deposit (rial)
        TransactionEntity userRialDeposit = createTransaction(userRialAccount, Double.parseDouble(price) - commissionValue,
                messageResolverService.resolve(depositTemplate, model), additionalData, purchaseRequest, rrn);
        transactionService.insertDeposit(userRialDeposit);

        // merchant withdrawal (currency)
        TransactionEntity merchantCurrencyWithdrawal = createTransaction( userCurrencyAccount, Double.parseDouble(amount),
                messageResolverService.resolve(withdrawalTemplate, model), additionalData, purchaseRequest, rrn);
        transactionService.insertWithdraw(merchantCurrencyWithdrawal);

        // user deposit (currency)
        TransactionEntity userCurrencyDeposit = createTransaction( merchantCurrencyAccount,Double.parseDouble(amount),
                messageResolverService.resolve(depositTemplate, model), additionalData, purchaseRequest, rrn);
        transactionService.insertDeposit(userCurrencyDeposit);
    }
}
