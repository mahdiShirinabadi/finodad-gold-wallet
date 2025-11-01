package com.melli.wallet.service.operation.impl;

import com.melli.wallet.domain.dto.BuyRequestDTO;
import com.melli.wallet.domain.dto.PurchaseObjectDto;
import com.melli.wallet.domain.dto.SellRequestDTO;
import com.melli.wallet.domain.enumaration.TransactionTypeEnum;
import com.melli.wallet.domain.master.entity.*;
import com.melli.wallet.domain.response.UuidResponse;
import com.melli.wallet.domain.response.purchase.PurchaseResponse;
import com.melli.wallet.domain.response.purchase.PurchaseTrackResponse;
import com.melli.wallet.domain.slave.entity.ReportPurchaseRequestEntity;
import com.melli.wallet.exception.InternalServiceException;
import com.melli.wallet.service.operation.PurchaseOperationService;
import com.melli.wallet.service.operation.WalletBuyLimitationOperationService;
import com.melli.wallet.service.operation.WalletOperationalService;
import com.melli.wallet.service.operation.WalletSellLimitationOperationService;
import com.melli.wallet.service.repository.*;
import com.melli.wallet.service.transactional.PurchaseTransactionalService;
import com.melli.wallet.utils.Helper;
import com.melli.wallet.utils.RedisLockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * Class Name: PurchaseServiceImplementation
 * Author: Mahdi Shirinabadi
 * Date: 1/27/2025
 */
@Service
@Log4j2
@RequiredArgsConstructor
public class PurchaseOperationServiceImplementation implements PurchaseOperationService {

    private final RedisLockService redisLockService;
    private final RrnRepositoryService rrnRepositoryService;
    private final RequestRepositoryService requestRepositoryService;
    private final MerchantRepositoryService merchantRepositoryService;
    private final Helper helper;
    private final WalletAccountRepositoryService walletAccountRepositoryService;
    private final StatusRepositoryService statusRepositoryService;
    private final WalletAccountCurrencyRepositoryService walletAccountCurrencyRepositoryService;
    private final RequestTypeRepositoryService requestTypeRepositoryService;
    private final WalletBuyLimitationOperationService walletBuyLimitationOperationService;
    private final WalletSellLimitationOperationService walletSellLimitationOperationService;
    private final PurchaseTransactionalService purchaseTransactionalService;
    private final WalletOperationalService walletOperationalService;

    @Override
    public UuidResponse buyGenerateUuid(BuyRequestDTO buyRequestDTO) throws InternalServiceException {
        try {
            log.info("start generate traceId, username ===> ({}), nationalCode ({})", buyRequestDTO.getChannel().getUsername(), buyRequestDTO.getNationalCode());
            WalletEntity walletEntity = walletOperationalService.findUserWallet(buyRequestDTO.getNationalCode());
            WalletAccountCurrencyEntity currencyEntity = walletAccountCurrencyRepositoryService.findCurrency(buyRequestDTO.getCurrency());
            WalletAccountEntity userCurrencyWalletAccount = walletAccountRepositoryService.checkUserAccount(walletEntity, currencyEntity, buyRequestDTO.getWalletAccountNumber(), buyRequestDTO.getNationalCode());
            RrnEntity rrnEntity = rrnRepositoryService.generateTraceId(buyRequestDTO.getNationalCode(), buyRequestDTO.getChannel(), requestTypeRepositoryService.getRequestType(TransactionTypeEnum.BUY.name()), buyRequestDTO.getWalletAccountNumber(), String.valueOf(buyRequestDTO.getPrice()));
            log.info("finish traceId ===> {}, username ({}), nationalCode ({})", rrnEntity.getUuid(), buyRequestDTO.getChannel().getUsername(), buyRequestDTO.getNationalCode());
            MerchantEntity merchant = merchantRepositoryService.findMerchant(buyRequestDTO.getMerchantId());

            if(merchant.getStatus() == MerchantRepositoryService.DISABLED){
                log.error("merchant is disable and system can not sell any things");
                throw new InternalServiceException("merchant is disable", StatusRepositoryService.MERCHANT_IS_DISABLE, HttpStatus.OK);
            }

            walletBuyLimitationOperationService.checkGeneral(buyRequestDTO.getChannel(), walletEntity, buyRequestDTO.getQuantity(), userCurrencyWalletAccount, rrnEntity.getUuid(), currencyEntity);
            walletBuyLimitationOperationService.checkDailyLimitation(buyRequestDTO.getChannel(), walletEntity, buyRequestDTO.getQuantity(), userCurrencyWalletAccount, rrnEntity.getUuid(), currencyEntity);

            walletBuyLimitationOperationService.checkMonthlyLimitation(buyRequestDTO.getChannel(), walletEntity, buyRequestDTO.getQuantity(), userCurrencyWalletAccount, rrnEntity.getUuid(), currencyEntity);


            WalletAccountEntity merchantCurrencyAccount = merchantRepositoryService.findMerchantWalletAccount(merchant, currencyEntity);
            if (walletAccountRepositoryService.getBalance(merchantCurrencyAccount.getId()).getAvailableBalance().compareTo(buyRequestDTO.getQuantity()) < 0) {
                log.error("balance for merchant id ({}) and walletAccount ({}) for currency ({}) is less than ({})", merchant.getId(), merchantCurrencyAccount.getId(), buyRequestDTO.getCurrency(), buyRequestDTO.getQuantity());
                throw new InternalServiceException("merchant balance is not enough", StatusRepositoryService.MERCHANT_BALANCE_NOT_ENOUGH, HttpStatus.OK);
            }

            return new UuidResponse(rrnEntity.getUuid());
        } catch (InternalServiceException e) {
            log.error("error in generate traceId with info ===> username ({}), nationalCode ({}) error ===> ({})", buyRequestDTO.getChannel().getUsername(), buyRequestDTO.getNationalCode(), e.getMessage());
            throw e;
        }
    }

    @Override
    public UuidResponse sellGenerateUuid(ChannelEntity channelEntity, String nationalCode, String quantity, String walletAccountNumber, String currency) throws InternalServiceException {
        try {
            log.info("start generate sell uuid, username ===> ({}), nationalCode ({})", channelEntity.getUsername(), nationalCode);
            WalletEntity walletEntity = walletOperationalService.findUserWallet(nationalCode);
            WalletAccountCurrencyEntity walletAccountCurrencyEntity = walletAccountCurrencyRepositoryService.findCurrency(currency);
            WalletAccountEntity walletAccountEntity = walletAccountRepositoryService.checkUserAccount(walletEntity, walletAccountCurrencyEntity, walletAccountNumber, nationalCode);
            RrnEntity rrnEntity = rrnRepositoryService.generateTraceId(nationalCode, channelEntity, requestTypeRepositoryService.getRequestType(TransactionTypeEnum.SELL.name()), walletAccountNumber, quantity);
            log.info("finish sell uuid ===> {}, username ({}), nationalCode ({})", rrnEntity.getUuid(), channelEntity.getUsername(), nationalCode);
            walletSellLimitationOperationService.checkGeneral(channelEntity, walletEntity, new BigDecimal(quantity), walletAccountEntity, walletAccountNumber);
            walletSellLimitationOperationService.checkDailyLimitation(channelEntity, walletEntity, new BigDecimal(quantity), walletAccountEntity, walletAccountNumber);
            walletSellLimitationOperationService.checkMonthlyLimitation(channelEntity, walletEntity, new BigDecimal(quantity), walletAccountEntity, walletAccountNumber);
            return new UuidResponse(rrnEntity.getUuid());
        } catch (InternalServiceException e) {
            log.error("error in generate sell uuid with info ===> username ({}), nationalCode ({}) error ===> ({})", channelEntity.getUsername(), nationalCode, e.getMessage());
            throw e;
        }
    }

    @Override
    public PurchaseResponse sell(SellRequestDTO sellRequestDTO) throws InternalServiceException {

        log.info("=== PURCHASE SELL OPERATION START ===");
        log.info("Input parameters - uniqueIdentifier: {}, nationalCode: {}, currency: {}, amount: {}, price: {}, commission: {}, merchantId: {}, walletAccountNumber: {}", 
            sellRequestDTO.getUniqueIdentifier(), sellRequestDTO.getNationalCode(), sellRequestDTO.getCurrency(), 
            sellRequestDTO.getAmount(), sellRequestDTO.getPrice(), sellRequestDTO.getCommission(), 
            sellRequestDTO.getMerchantId(), sellRequestDTO.getWalletAccountNumber());

        log.info("=== CURRENCY VALIDATION ===");
        if (!sellRequestDTO.getCurrency().equalsIgnoreCase(sellRequestDTO.getCommissionCurrency())) {
            log.error("Commission currency validation failed - currency: {}, commissionCurrency: {}", 
                sellRequestDTO.getCurrency(), sellRequestDTO.getCommissionCurrency());
            throw new InternalServiceException("commission and currency not be same", StatusRepositoryService.COMMISSION_CURRENCY_NOT_VALID, HttpStatus.OK);
        }
        log.info("Commission currency validation passed - currency: {}", sellRequestDTO.getCurrency());
        
        WalletAccountCurrencyEntity currencyEntity = walletAccountCurrencyRepositoryService.findCurrency(sellRequestDTO.getCurrency());
        log.info("Currency entity found - currency: {}, id: {}", currencyEntity.getName(), currencyEntity.getId());

        WalletAccountCurrencyEntity rialCurrencyEntity = walletAccountCurrencyRepositoryService.findCurrency(WalletAccountCurrencyRepositoryService.RIAL);
        log.info("Rial currency entity found - currency: {}, id: {}", rialCurrencyEntity.getName(), rialCurrencyEntity.getId());

        BigDecimal netAmount = sellRequestDTO.getAmount().subtract(sellRequestDTO.getCommission());
        log.info("Calculating net amount - amount: {}, commission: {}, netAmount: {}",
            sellRequestDTO.getAmount(), sellRequestDTO.getCommission(), netAmount);
        
        if(netAmount.compareTo(new BigDecimal("0")) <= 0){
            log.error("Commission validation failed - commission: {} is bigger than or equal to amount: {}", 
                sellRequestDTO.getCommission(), sellRequestDTO.getAmount());
            throw new InternalServiceException("commission is bigger than quantity", StatusRepositoryService.COMMISSION_BIGGER_THAN_QUANTITY, HttpStatus.OK);
        }
        log.info("Commission validation passed - netAmount: {}", netAmount);

        log.info("=== MERCHANT VALIDATION ===");
        log.debug("Validating merchant - merchantId: {}", sellRequestDTO.getMerchantId());
        MerchantEntity merchant = merchantRepositoryService.findMerchant(sellRequestDTO.getMerchantId());
        log.info("Merchant found - id: {}, name: {}, status: {}", 
            merchant.getId(), merchant.getName(), merchant.getStatus());

        if(merchant.getStatus() == MerchantRepositoryService.DISABLED){
            log.error("Merchant status validation failed - merchant is disabled - merchantId: {}, status: {}", 
                merchant.getId(), merchant.getStatus());
            throw new InternalServiceException("merchant is disable", StatusRepositoryService.MERCHANT_IS_DISABLE, HttpStatus.OK);
        }
        log.info("Merchant status validation passed - merchant is active");
        
        log.debug("Retrieving merchant wallet accounts");
        WalletAccountEntity merchantCurrencyAccount = merchantRepositoryService.findMerchantWalletAccount(merchant, currencyEntity);
        WalletAccountEntity merchantRialAccount = merchantRepositoryService.findMerchantWalletAccount(merchant, rialCurrencyEntity);
        log.info("Merchant accounts found - currencyAccount: {}, rialAccount: {}", 
            merchantCurrencyAccount.getId(), merchantRialAccount.getId());

        log.info("=== USER VALIDATION ===");
        log.debug("Validating user wallet - nationalCode: {}", sellRequestDTO.getNationalCode());
        WalletEntity userWallet = walletOperationalService.findUserWallet(sellRequestDTO.getNationalCode());
        log.info("User wallet found - walletId: {}, nationalCode: {}", 
            userWallet.getId(), userWallet.getNationalCode());


        
        log.debug("Retrieving user wallet accounts");
        WalletAccountEntity userRialAccount = walletAccountRepositoryService.findUserWalletAccount(userWallet, rialCurrencyEntity, sellRequestDTO.getCurrency());
        WalletAccountEntity userCurrencyAccount = walletAccountRepositoryService.checkUserAccount(userWallet, currencyEntity, sellRequestDTO.getWalletAccountNumber(), sellRequestDTO.getNationalCode());
        log.info("User accounts found - rialAccount: {}, currencyAccount: {}", 
            userRialAccount.getId(), userCurrencyAccount.getId());

        walletSellLimitationOperationService.checkGeneral(sellRequestDTO.getChannel(), userWallet, sellRequestDTO.getAmount(), userCurrencyAccount, sellRequestDTO.getWalletAccountNumber());
        walletSellLimitationOperationService.checkDailyLimitation(sellRequestDTO.getChannel(), userWallet, sellRequestDTO.getAmount(), userCurrencyAccount, sellRequestDTO.getWalletAccountNumber());
        walletSellLimitationOperationService.checkMonthlyLimitation(sellRequestDTO.getChannel(), userWallet, sellRequestDTO.getAmount(), userCurrencyAccount, sellRequestDTO.getWalletAccountNumber());

        log.info("=== CHANNEL COMMISSION VALIDATION ===");
        WalletAccountEntity channelCommissionAccount = walletAccountRepositoryService.findChannelCommissionAccount(sellRequestDTO.getChannel(), sellRequestDTO.getCommissionCurrency());
        log.info("Channel commission account found - accountId: {}, accountNumber: {}, currency: {}", 
            channelCommissionAccount.getId(), channelCommissionAccount.getAccountNumber(), sellRequestDTO.getCommissionCurrency());

        log.info("=== STARTING TRANSACTIONAL SELL PROCESS ===");
        log.info("Starting Redis lock acquisition for walletAccount: {}", sellRequestDTO.getWalletAccountNumber());
        
        return redisLockService.runWithLockUntilCommit(sellRequestDTO.getWalletAccountNumber(), this.getClass(), () -> {
            log.info("=== LOCK ACQUIRED - STARTING SELL TRANSACTIONAL PROCESS ===");
            log.info("Creating PurchaseObjectDto for sell transaction");
            
            PurchaseObjectDto purchaseObject = new PurchaseObjectDto(
                sellRequestDTO.getChannel(),
                sellRequestDTO.getUniqueIdentifier(),
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
                channelCommissionAccount
            );
            
            log.info("PurchaseObjectDto created - amount: {}, price: {}, commission: {}", 
                purchaseObject.getQuantity(), purchaseObject.getPrice(), purchaseObject.getCommission());
            
            log.info("Calling purchaseTransactionalService.processSell");
            PurchaseResponse response = purchaseTransactionalService.processSell(purchaseObject);
            
            log.info("Sell transaction completed successfully - response: {}", response);
            log.info("=== SELL TRANSACTIONAL PROCESS COMPLETED ===");
            
            return response;
        }, sellRequestDTO.getNationalCode());
    }

    @Override
    public PurchaseTrackResponse purchaseTrack(ChannelEntity channel, String uniqueIdentifier, String type, String channelIp) throws InternalServiceException {
        RrnEntity rrnEntity = rrnRepositoryService.checkRrn(uniqueIdentifier, channel, requestTypeRepositoryService.getRequestType(type), "", "");
        ReportPurchaseRequestEntity purchaseRequestEntity = requestRepositoryService.findPurchaseRequestByRrnId(rrnEntity.getId());
        return helper.fillPurchaseTrackResponse(purchaseRequestEntity, statusRepositoryService);
    }

    @Override
    public PurchaseResponse buy(BuyRequestDTO buyRequestDTO) throws InternalServiceException {

        log.info("=== PURCHASE BUY OPERATION START ===");
        log.info("Input parameters - uniqueIdentifier: {}, nationalCode: {}, currency: {}, quantity: {}, price: {}, merchantId: {}, walletAccountNumber: {}", 
            buyRequestDTO.getUniqueIdentifier(), buyRequestDTO.getNationalCode(), buyRequestDTO.getCurrency(), 
            buyRequestDTO.getQuantity(), buyRequestDTO.getPrice(), buyRequestDTO.getMerchantId(), buyRequestDTO.getWalletAccountNumber());

        log.info("=== CURRENCY VALIDATION ===");
        log.debug("Validating and retrieving currencies - currency: {}, commissionType: {}", 
            buyRequestDTO.getCurrency(), buyRequestDTO.getCommissionType());
        
        WalletAccountCurrencyEntity currencyEntity = walletAccountCurrencyRepositoryService.findCurrency(buyRequestDTO.getCurrency());
        log.info("Currency entity found - currency: {}, id: {}", currencyEntity.getName(), currencyEntity.getId());

        WalletAccountCurrencyEntity rialCurrencyEntity = walletAccountCurrencyRepositoryService.findCurrency(WalletAccountCurrencyRepositoryService.RIAL);
        log.info("Rial currency entity found - currency: {}, id: {}", rialCurrencyEntity.getName(), rialCurrencyEntity.getId());

        log.info("=== MERCHANT VALIDATION ===");
        log.debug("Validating merchant - merchantId: {}", buyRequestDTO.getMerchantId());
        MerchantEntity merchant = merchantRepositoryService.findMerchant(buyRequestDTO.getMerchantId());
        log.info("Merchant found - id: {}, name: {}, status: {}", 
            merchant.getId(), merchant.getName(), merchant.getStatus());
        
        log.debug("Retrieving merchant wallet accounts");
        WalletAccountEntity merchantCurrencyAccount = merchantRepositoryService.findMerchantWalletAccount(merchant, currencyEntity);
        WalletAccountEntity merchantRialAccount = merchantRepositoryService.findMerchantWalletAccount(merchant, rialCurrencyEntity);
        log.info("Merchant accounts found - currencyAccount: {}, rialAccount: {}", 
            merchantCurrencyAccount.getId(), merchantRialAccount.getId());

        log.info("=== USER VALIDATION ===");
        log.debug("Validating user wallet - nationalCode: {}", buyRequestDTO.getNationalCode());
        WalletEntity userWallet = walletOperationalService.findUserWallet(buyRequestDTO.getNationalCode());
        log.info("User wallet found - walletId: {}, nationalCode: {}", 
            userWallet.getId(), userWallet.getNationalCode());
        
        log.debug("Retrieving user wallet accounts");
        WalletAccountEntity userCurrencyAccount = walletAccountRepositoryService.findUserWalletAccount(userWallet, currencyEntity, buyRequestDTO.getCurrency());
        WalletAccountEntity userRialAccount = walletAccountRepositoryService.findUserAccount(userWallet, rialCurrencyEntity, buyRequestDTO.getNationalCode());
        log.info("User accounts found - currencyAccount: {}, rialAccount: {}", 
            userCurrencyAccount.getId(), userRialAccount.getId());

        log.info("=== COMMISSION VALIDATION ===");
        if (!WalletAccountCurrencyRepositoryService.RIAL.equalsIgnoreCase(buyRequestDTO.getCommissionType())) {
            log.error("Commission type validation failed - expected: RIAL, actual: {}", buyRequestDTO.getCommissionType());
            throw new InternalServiceException("commission type must be rial", StatusRepositoryService.COMMISSION_CURRENCY_NOT_VALID, HttpStatus.OK);
        }
        log.info("Commission type validation passed - type: RIAL");
        
        WalletAccountEntity channelCommissionAccount = walletAccountRepositoryService.findChannelCommissionAccount(buyRequestDTO.getChannel(), WalletAccountCurrencyRepositoryService.RIAL);
        log.info("Channel commission account found - accountId: {}, accountNumber: {}", 
            channelCommissionAccount.getId(), channelCommissionAccount.getAccountNumber());

        log.info("=== LIMITATION CHECKS ===");
        log.debug("Checking buy limitations - quantity: {}, currency: {}", buyRequestDTO.getQuantity(), buyRequestDTO.getCurrency());
        walletBuyLimitationOperationService.checkGeneral(buyRequestDTO.getChannel(), userCurrencyAccount.getWalletEntity(), buyRequestDTO.getQuantity(), userCurrencyAccount, buyRequestDTO.getUniqueIdentifier(), currencyEntity);
        log.info("General buy limitation check passed");
        
        walletBuyLimitationOperationService.checkDailyLimitation(buyRequestDTO.getChannel(), userCurrencyAccount.getWalletEntity(), buyRequestDTO.getQuantity(), userCurrencyAccount, buyRequestDTO.getUniqueIdentifier(), currencyEntity);
        log.info("Daily buy limitation check passed");
        
        walletBuyLimitationOperationService.checkMonthlyLimitation(buyRequestDTO.getChannel(), userCurrencyAccount.getWalletEntity(), buyRequestDTO.getQuantity(), userCurrencyAccount, buyRequestDTO.getUniqueIdentifier(), currencyEntity);
        log.info("Monthly buy limitation check passed");

        log.info("=== STARTING TRANSACTIONAL BUY PROCESS ===");
        log.info("Starting Redis lock acquisition for walletAccount: {}", buyRequestDTO.getWalletAccountNumber());
        
        return redisLockService.runAfterLock(buyRequestDTO.getWalletAccountNumber(), this.getClass(), () -> {
            log.info("=== LOCK ACQUIRED - STARTING BUY TRANSACTIONAL PROCESS ===");
            log.info("Creating PurchaseObjectDto for buy transaction");
            
            PurchaseObjectDto purchaseObject = new PurchaseObjectDto(
                buyRequestDTO.getChannel(),
                buyRequestDTO.getUniqueIdentifier(),
                buyRequestDTO.getQuantity(),
                BigDecimal.valueOf(buyRequestDTO.getPrice()),
                buyRequestDTO.getCommission(),
                buyRequestDTO.getAdditionalData(),
                buyRequestDTO.getNationalCode(),
                userWallet,
                userRialAccount,
                userCurrencyAccount,
                merchant,
                merchantRialAccount,
                merchantCurrencyAccount,
                channelCommissionAccount
            );
            
            log.info("PurchaseObjectDto created - quantity: {}, price: {}, commission: {}", 
                purchaseObject.getQuantity(), purchaseObject.getPrice(), purchaseObject.getCommission());
            
            log.info("Calling purchaseTransactionalService.processBuy");
            PurchaseResponse response = purchaseTransactionalService.processBuy(purchaseObject, null, null, false);
            
            log.info("Buy transaction completed successfully - response: {}", response);
            log.info("=== BUY TRANSACTIONAL PROCESS COMPLETED ===");
            
            return response;
        }, buyRequestDTO.getUniqueIdentifier());
    }


    public PurchaseResponse buyDirect(BuyRequestDTO buyRequestDTO) throws InternalServiceException {

        // Validate and retrieve currencies
        WalletAccountCurrencyEntity currencyEntity = walletAccountCurrencyRepositoryService.findCurrency(buyRequestDTO.getCurrency());
        WalletAccountCurrencyEntity rialCurrencyEntity = walletAccountCurrencyRepositoryService.findCurrency(WalletAccountCurrencyRepositoryService.RIAL);

        MerchantEntity merchant = merchantRepositoryService.findMerchant(buyRequestDTO.getMerchantId());
        WalletAccountEntity merchantRialAccount = merchantRepositoryService.findMerchantWalletAccount(merchant, rialCurrencyEntity);

        // Validate merchant and wallet accounts
        WalletAccountEntity merchantCurrencyAccount = merchantRepositoryService.findMerchantWalletAccount(merchant, currencyEntity);
        if (walletAccountRepositoryService.getBalance(merchantCurrencyAccount.getId()).getAvailableBalance().compareTo(buyRequestDTO.getQuantity()) < 0) {
            log.error("balance for merchant id ({}) and walletAccount ({}) for currency ({}) is less than ({})", merchant.getId(), merchantCurrencyAccount.getId(), buyRequestDTO.getCurrency(), buyRequestDTO.getQuantity());
            throw new InternalServiceException("merchant balance is not enough", StatusRepositoryService.MERCHANT_BALANCE_NOT_ENOUGH, HttpStatus.OK);
        }

        if((new BigDecimal(buyRequestDTO.getPrice()).subtract(buyRequestDTO.getCommission())).compareTo(new BigDecimal("0")) <= 0){
            log.error("commission ({}) is bigger than price ({})", buyRequestDTO.getCommission(), buyRequestDTO.getPrice());
            throw new InternalServiceException("commission is bigger than quantity", StatusRepositoryService.COMMISSION_BIGGER_THAN_QUANTITY, HttpStatus.OK);
        }

        // Validate user and wallet accounts
        WalletEntity userWallet = walletOperationalService.findUserWallet(buyRequestDTO.getNationalCode());
        WalletAccountEntity userCurrencyAccount = walletAccountRepositoryService.findUserWalletAccount(userWallet, currencyEntity, buyRequestDTO.getCurrency());
        WalletAccountEntity userRialAccount = walletAccountRepositoryService.findUserAccount(userWallet, rialCurrencyEntity, buyRequestDTO.getNationalCode());


        //first charge rial account and after that purchase (only in accountToAccount)
        log.info("start generate traceId in direct buy, username ===> ({}), nationalCode ({})", buyRequestDTO.getChannel().getUsername(), buyRequestDTO.getNationalCode());
        RrnEntity rrnEntityForCharge = rrnRepositoryService.generateTraceId(buyRequestDTO.getNationalCode(), buyRequestDTO.getChannel(), requestTypeRepositoryService.getRequestType(RequestTypeRepositoryService.CASH_IN), userRialAccount.getAccountNumber(), String.valueOf(buyRequestDTO.getPrice() + buyRequestDTO.getCommission().longValue()));
        log.info("finish direct buy traceId ===> {}, username ({}), nationalCode ({})", rrnEntityForCharge.getUuid(), buyRequestDTO.getChannel().getUsername(), buyRequestDTO.getNationalCode());

        // Validate channel commission account
        if (!WalletAccountCurrencyRepositoryService.RIAL.equalsIgnoreCase(buyRequestDTO.getCommissionType())) {
            log.error("commission type in buy must it rial!!!");
            throw new InternalServiceException("commission type must be rial", StatusRepositoryService.COMMISSION_CURRENCY_NOT_VALID, HttpStatus.OK);
        }

        WalletAccountEntity channelCommissionAccount = walletAccountRepositoryService.findChannelCommissionAccount(buyRequestDTO.getChannel(), WalletAccountCurrencyRepositoryService.RIAL);

        walletBuyLimitationOperationService.checkGeneral(buyRequestDTO.getChannel(), userCurrencyAccount.getWalletEntity(), buyRequestDTO.getQuantity(), userCurrencyAccount, buyRequestDTO.getUniqueIdentifier(), walletAccountCurrencyRepositoryService.findCurrency(buyRequestDTO.getCurrency()));
        walletBuyLimitationOperationService.checkDailyLimitation(buyRequestDTO.getChannel(), userCurrencyAccount.getWalletEntity(), buyRequestDTO.getQuantity(), userCurrencyAccount, buyRequestDTO.getUniqueIdentifier(), walletAccountCurrencyRepositoryService.findCurrency(buyRequestDTO.getCurrency()));
        walletBuyLimitationOperationService.checkMonthlyLimitation(buyRequestDTO.getChannel(), userCurrencyAccount.getWalletEntity(), buyRequestDTO.getQuantity(), userCurrencyAccount, buyRequestDTO.getUniqueIdentifier(), walletAccountCurrencyRepositoryService.findCurrency(buyRequestDTO.getCurrency()));

        return redisLockService.runWithLockUntilCommit(buyRequestDTO.getWalletAccountNumber(), this.getClass(), () -> purchaseTransactionalService.processBuy(new PurchaseObjectDto(
                        buyRequestDTO.getChannel(),
                        buyRequestDTO.getUniqueIdentifier(),
                        buyRequestDTO.getQuantity(),
                        BigDecimal.valueOf(buyRequestDTO.getPrice()),
                        buyRequestDTO.getCommission(),
                        buyRequestDTO.getAdditionalData(),
                        buyRequestDTO.getNationalCode(),
                        userWallet,
                        userRialAccount,
                        userCurrencyAccount,
                        merchant,
                        merchantRialAccount,
                        merchantCurrencyAccount,
                        channelCommissionAccount)
                , rrnEntityForCharge.getUuid(), buyRequestDTO.getRefNumber(), true), buyRequestDTO.getUniqueIdentifier());
    }


}
