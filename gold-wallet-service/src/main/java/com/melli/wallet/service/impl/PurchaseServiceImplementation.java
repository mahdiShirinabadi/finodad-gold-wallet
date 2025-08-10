package com.melli.wallet.service.impl;

import com.melli.wallet.domain.dto.BuyRequestDTO;
import com.melli.wallet.domain.dto.PurchaseObjectDto;
import com.melli.wallet.domain.dto.SellRequestDTO;
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

import java.math.BigDecimal;
import java.util.List;

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
    private final MerchantService merchantService;
    private final Helper helper;
    private final WalletService walletService;
    private final WalletAccountService walletAccountService;
    private final WalletTypeService walletTypeService;
    private final StatusService statusService;
    private final WalletAccountTypeService walletAccountTypeService;
    private final WalletAccountCurrencyService walletAccountCurrencyService;
    private final RequestTypeService requestTypeService;
    private final WalletBuyLimitationService walletBuyLimitationService;
    private final WalletSellLimitationService walletSellLimitationService;
    private final PurchaseTransactionalService purchaseTransactionalService;

    @Override
    public UuidResponse buyGenerateUuid(BuyRequestDTO buyRequestDTO) throws InternalServiceException {
        try {
            log.info("start generate traceId, username ===> ({}), nationalCode ({})", buyRequestDTO.getChannel().getUsername(), buyRequestDTO.getNationalCode());
            WalletEntity walletEntity = findUserWallet(buyRequestDTO.getNationalCode());
            WalletAccountCurrencyEntity currencyEntity = walletAccountCurrencyService.findCurrency(buyRequestDTO.getCurrency());
            WalletAccountEntity userCurrencyWalletAccount = checkUserAccount(walletEntity, currencyEntity, buyRequestDTO.getWalletAccountNumber(), buyRequestDTO.getNationalCode());
            RrnEntity rrnEntity = rrnService.generateTraceId(buyRequestDTO.getNationalCode(), buyRequestDTO.getChannel(), requestTypeService.getRequestType(TransactionTypeEnum.BUY.name()), buyRequestDTO.getWalletAccountNumber(), String.valueOf(buyRequestDTO.getPrice()));
            log.info("finish traceId ===> {}, username ({}), nationalCode ({})", rrnEntity.getUuid(), buyRequestDTO.getChannel().getUsername(), buyRequestDTO.getNationalCode());
            MerchantEntity merchant = findMerchant(buyRequestDTO.getMerchantId());


            walletBuyLimitationService.checkGeneral(buyRequestDTO.getChannel(), walletEntity, buyRequestDTO.getAmount(), userCurrencyWalletAccount, rrnEntity.getUuid(), currencyEntity);
            walletBuyLimitationService.checkDailyLimitation(buyRequestDTO.getChannel(), walletEntity, buyRequestDTO.getAmount(), userCurrencyWalletAccount, rrnEntity.getUuid(), currencyEntity);

            walletBuyLimitationService.checkMonthlyLimitation(buyRequestDTO.getChannel(), walletEntity, buyRequestDTO.getAmount(), userCurrencyWalletAccount, rrnEntity.getUuid(), currencyEntity);


            WalletAccountEntity merchantCurrencyAccount = findMerchantWalletAccount(merchant, currencyEntity);
            if (walletAccountService.getBalance(merchantCurrencyAccount.getId()).compareTo(buyRequestDTO.getAmount()) < 0) {
                log.error("balance for merchant id ({}) and walletAccount ({}) for currency ({}) is less than ({})", merchant.getId(), merchantCurrencyAccount.getId(), buyRequestDTO.getCurrency(), buyRequestDTO.getAmount());
                throw new InternalServiceException("merchant balance is not enough", StatusService.MERCHANT_BALANCE_NOT_ENOUGH, HttpStatus.OK);
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
            WalletEntity walletEntity = findUserWallet(nationalCode);
            WalletAccountCurrencyEntity walletAccountCurrencyEntity = walletAccountCurrencyService.findCurrency(currency);
            WalletAccountEntity walletAccountEntity = checkUserAccount(walletEntity, walletAccountCurrencyEntity, walletAccountNumber, nationalCode);
            RrnEntity rrnEntity = rrnService.generateTraceId(nationalCode, channelEntity, requestTypeService.getRequestType(TransactionTypeEnum.SELL.name()), walletAccountNumber, quantity);
            log.info("finish sell uuid ===> {}, username ({}), nationalCode ({})", rrnEntity.getUuid(), channelEntity.getUsername(), nationalCode);
            walletSellLimitationService.checkGeneral(channelEntity, walletEntity, new BigDecimal(quantity), walletAccountEntity, walletAccountNumber);
            walletSellLimitationService.checkDailyLimitation(channelEntity, walletEntity, new BigDecimal(quantity), walletAccountEntity, walletAccountNumber);
            walletSellLimitationService.checkMonthlyLimitation(channelEntity, walletEntity, new BigDecimal(quantity), walletAccountEntity, walletAccountNumber);
            return new UuidResponse(rrnEntity.getUuid());
        } catch (InternalServiceException e) {
            log.error("error in generate sell uuid with info ===> username ({}), nationalCode ({}) error ===> ({})", channelEntity.getUsername(), nationalCode, e.getMessage());
            throw e;
        }
    }

    @Override
    public PurchaseResponse sell(SellRequestDTO sellRequestDTO) throws InternalServiceException {

        if (!sellRequestDTO.getCurrency().equalsIgnoreCase(sellRequestDTO.getCommissionCurrency())) {
            log.error("commission and currency not be same!!!");
            throw new InternalServiceException("commission and currency not be same", StatusService.COMMISSION_CURRENCY_NOT_VALID, HttpStatus.OK);
        }

        // Validate and retrieve currencies
        WalletAccountCurrencyEntity currencyEntity = walletAccountCurrencyService.findCurrency(sellRequestDTO.getCurrency());
        WalletAccountCurrencyEntity rialCurrencyEntity = walletAccountCurrencyService.findCurrency(WalletAccountCurrencyService.RIAL);


        // Validate merchant and wallet accounts
        MerchantEntity merchant = findMerchant(sellRequestDTO.getMerchantId());
        WalletAccountEntity merchantCurrencyAccount = findMerchantWalletAccount(merchant, currencyEntity);
        WalletAccountEntity merchantRialAccount = findMerchantWalletAccount(merchant, rialCurrencyEntity);

        // Validate user and wallet accounts
        WalletEntity userWallet = findUserWallet(sellRequestDTO.getNationalCode());
        WalletAccountEntity userRialAccount = findUserWalletAccount(userWallet, rialCurrencyEntity, sellRequestDTO.getCurrency());
        WalletAccountEntity userCurrencyAccount = checkUserAccount(userWallet, currencyEntity, sellRequestDTO.getWalletAccountNumber(), sellRequestDTO.getNationalCode());

        // Validate channel commission account
        WalletAccountEntity channelCommissionAccount = findChannelCommissionAccount(sellRequestDTO.getChannel(), sellRequestDTO.getCommissionCurrency());

        return redisLockService.runAfterLock(sellRequestDTO.getWalletAccountNumber(), this.getClass(), () -> purchaseTransactionalService.processSell(new PurchaseObjectDto(
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
                channelCommissionAccount)
        ), sellRequestDTO.getNationalCode());
    }

    @Override
    public PurchaseTrackResponse purchaseTrack(ChannelEntity channel, String uniqueIdentifier, String type, String channelIp) throws InternalServiceException {
        RrnEntity rrnEntity = rrnService.checkRrn(uniqueIdentifier, channel, requestTypeService.getRequestType(type), "", "");
        PurchaseRequestEntity purchaseRequestEntity = requestService.findPurchaseRequestByRrnId(rrnEntity.getId());
        return helper.fillPurchaseTrackResponse(purchaseRequestEntity, statusService);
    }

    @Override
    public PurchaseResponse buy(BuyRequestDTO buyRequestDTO) throws InternalServiceException {

        // Validate and retrieve currencies
        WalletAccountCurrencyEntity currencyEntity = walletAccountCurrencyService.findCurrency(buyRequestDTO.getCurrency());


        WalletAccountCurrencyEntity rialCurrencyEntity = walletAccountCurrencyService.findCurrency(WalletAccountCurrencyService.RIAL);


        // Validate merchant and wallet accounts
        MerchantEntity merchant = findMerchant(buyRequestDTO.getMerchantId());
        WalletAccountEntity merchantCurrencyAccount = findMerchantWalletAccount(merchant, currencyEntity);
        WalletAccountEntity merchantRialAccount = findMerchantWalletAccount(merchant, rialCurrencyEntity);

        // Validate user and wallet accounts
        WalletEntity userWallet = findUserWallet(buyRequestDTO.getNationalCode());
        WalletAccountEntity userCurrencyAccount = findUserWalletAccount(userWallet, currencyEntity, buyRequestDTO.getCurrency());
        WalletAccountEntity userRialAccount = findUserAccount(userWallet, rialCurrencyEntity, buyRequestDTO.getNationalCode());

        // Validate channel commission account
        if (!WalletAccountCurrencyService.RIAL.equalsIgnoreCase(buyRequestDTO.getCommissionType())) {
            log.error("commission type in buy must it rial!!!");
            throw new InternalServiceException("commission type must be rial", StatusService.COMMISSION_CURRENCY_NOT_VALID, HttpStatus.OK);
        }
        WalletAccountEntity channelCommissionAccount = findChannelCommissionAccount(buyRequestDTO.getChannel(), WalletAccountCurrencyService.RIAL);

        walletBuyLimitationService.checkGeneral(buyRequestDTO.getChannel(), userCurrencyAccount.getWalletEntity(), buyRequestDTO.getAmount(), userCurrencyAccount, buyRequestDTO.getUniqueIdentifier(), currencyEntity);
        walletBuyLimitationService.checkDailyLimitation(buyRequestDTO.getChannel(), userCurrencyAccount.getWalletEntity(), buyRequestDTO.getAmount(), userCurrencyAccount, buyRequestDTO.getUniqueIdentifier(), currencyEntity);
        walletBuyLimitationService.checkMonthlyLimitation(buyRequestDTO.getChannel(), userCurrencyAccount.getWalletEntity(), buyRequestDTO.getAmount(), userCurrencyAccount, buyRequestDTO.getUniqueIdentifier(), currencyEntity);

        return redisLockService.runAfterLock(buyRequestDTO.getWalletAccountNumber(), this.getClass(), () -> purchaseTransactionalService.processBuy(new PurchaseObjectDto(
                buyRequestDTO.getChannel(),
                buyRequestDTO.getUniqueIdentifier(),
                buyRequestDTO.getAmount(),
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
                channelCommissionAccount), null, null, false
        ), buyRequestDTO.getUniqueIdentifier());
    }


    public PurchaseResponse buyDirect(BuyRequestDTO buyRequestDTO) throws InternalServiceException {

        // Validate and retrieve currencies
        WalletAccountCurrencyEntity currencyEntity = walletAccountCurrencyService.findCurrency(buyRequestDTO.getCurrency());
        WalletAccountCurrencyEntity rialCurrencyEntity = walletAccountCurrencyService.findCurrency(WalletAccountCurrencyService.RIAL);

        MerchantEntity merchant = findMerchant(buyRequestDTO.getMerchantId());
        WalletAccountEntity merchantRialAccount = findMerchantWalletAccount(merchant, rialCurrencyEntity);

        // Validate merchant and wallet accounts
        WalletAccountEntity merchantCurrencyAccount = findMerchantWalletAccount(merchant, currencyEntity);
        if (walletAccountService.getBalance(merchantCurrencyAccount.getId()).compareTo(buyRequestDTO.getAmount()) < 0) {
            log.error("balance for merchant id ({}) and walletAccount ({}) for currency ({}) is less than ({})", merchant.getId(), merchantCurrencyAccount.getId(), buyRequestDTO.getCurrency(), buyRequestDTO.getAmount());
            throw new InternalServiceException("merchant balance is not enough", StatusService.MERCHANT_BALANCE_NOT_ENOUGH, HttpStatus.OK);
        }


        // Validate user and wallet accounts
        WalletEntity userWallet = findUserWallet(buyRequestDTO.getNationalCode());
        WalletAccountEntity userCurrencyAccount = findUserWalletAccount(userWallet, currencyEntity, buyRequestDTO.getCurrency());
        WalletAccountEntity userRialAccount = findUserAccount(userWallet, rialCurrencyEntity, buyRequestDTO.getNationalCode());


        //first charge rial account and after that purchase (only in accountToAccount)
        log.info("start generate traceId in direct buy, username ===> ({}), nationalCode ({})", buyRequestDTO.getChannel().getUsername(), buyRequestDTO.getNationalCode());
        RrnEntity rrnEntityForCharge = rrnService.generateTraceId(buyRequestDTO.getNationalCode(), buyRequestDTO.getChannel(), requestTypeService.getRequestType(RequestTypeService.CASH_IN), userRialAccount.getAccountNumber(), String.valueOf(buyRequestDTO.getPrice()));
        log.info("finish direct buy traceId ===> {}, username ({}), nationalCode ({})", rrnEntityForCharge.getUuid(), buyRequestDTO.getChannel().getUsername(), buyRequestDTO.getNationalCode());

        // Validate channel commission account
        if (!WalletAccountCurrencyService.RIAL.equalsIgnoreCase(buyRequestDTO.getCommissionType())) {
            log.error("commission type in buy must it rial!!!");
            throw new InternalServiceException("commission type must be rial", StatusService.COMMISSION_CURRENCY_NOT_VALID, HttpStatus.OK);
        }

        WalletAccountEntity channelCommissionAccount = findChannelCommissionAccount(buyRequestDTO.getChannel(), WalletAccountCurrencyService.RIAL);

        walletBuyLimitationService.checkGeneral(buyRequestDTO.getChannel(), userCurrencyAccount.getWalletEntity(), buyRequestDTO.getAmount(), userCurrencyAccount, buyRequestDTO.getUniqueIdentifier(), walletAccountCurrencyService.findCurrency(buyRequestDTO.getCurrency()));
        walletBuyLimitationService.checkDailyLimitation(buyRequestDTO.getChannel(), userCurrencyAccount.getWalletEntity(), buyRequestDTO.getAmount(), userCurrencyAccount, buyRequestDTO.getUniqueIdentifier(), walletAccountCurrencyService.findCurrency(buyRequestDTO.getCurrency()));
        walletBuyLimitationService.checkMonthlyLimitation(buyRequestDTO.getChannel(), userCurrencyAccount.getWalletEntity(), buyRequestDTO.getAmount(), userCurrencyAccount, buyRequestDTO.getUniqueIdentifier(), walletAccountCurrencyService.findCurrency(buyRequestDTO.getCurrency()));

        return redisLockService.runAfterLock(buyRequestDTO.getWalletAccountNumber(), this.getClass(), () -> purchaseTransactionalService.processBuy(new PurchaseObjectDto(
                        buyRequestDTO.getChannel(),
                        buyRequestDTO.getUniqueIdentifier(),
                        buyRequestDTO.getAmount(),
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
            MerchantEntity merchant, WalletAccountCurrencyEntity currencyEntity) throws InternalServiceException {
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
                    log.error("Wallet account with currency {} not found for merchant {}", currencyEntity.getName(), merchant.getName());
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

    private WalletAccountEntity checkUserAccount(WalletEntity wallet, WalletAccountCurrencyEntity currencyEntity, String walletAccountNumber, String nationalCode) throws InternalServiceException {
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

    private WalletAccountEntity findUserAccount(WalletEntity wallet, WalletAccountCurrencyEntity currencyEntity, String nationalCode) throws InternalServiceException {
        List<WalletAccountEntity> accounts = walletAccountService.findByWallet(wallet);
        if (CollectionUtils.isEmpty(accounts)) {
            log.error("No wallet accounts found for user {}", wallet);
            throw new InternalServiceException("Wallet account not found for user", StatusService.WALLET_ACCOUNT_NOT_FOUND, HttpStatus.OK);
        }
        return accounts.stream().filter(x -> x.getWalletAccountCurrencyEntity().getId() == (currencyEntity.getId()) && x.getEndTime() == null).findFirst().orElseThrow(() -> {
            log.error("Rial wallet account not found for user {}", nationalCode);
            return new InternalServiceException("Wallet account not found for user", StatusService.WALLET_ACCOUNT_NOT_FOUND, HttpStatus.OK);
        });
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


}
