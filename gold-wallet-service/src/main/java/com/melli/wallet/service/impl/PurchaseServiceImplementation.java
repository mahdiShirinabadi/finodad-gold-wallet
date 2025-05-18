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
    private final MerchantService merchantService;
    private final Helper helper;
    private final WalletService walletService;
    private final WalletAccountService walletAccountService;
    private final WalletTypeService walletTypeService;
    private final TransactionService transactionService;
    private final MessageResolverService messageResolverService;
    private final TemplateService templateService;
    private final StatusService statusService;
    private final WalletAccountTypeService walletAccountTypeService;
    private final WalletAccountCurrencyService walletAccountCurrencyService;
    private final RequestTypeService requestTypeService;
    private final WalletBuyLimitationService walletBuyLimitationService;

    @Override
    public UuidResponse generateUuid(ChannelEntity channelEntity, String nationalCode, String price, String walletAccountNumber, String type) throws InternalServiceException {
        try {
            log.info("start generate traceId, username ===> ({}), nationalCode ({})", channelEntity.getUsername(), nationalCode);
            WalletEntity walletEntity = findUserWallet(nationalCode);
            WalletAccountCurrencyEntity rialCurrencyEntity = walletAccountCurrencyService.findCurrency(WalletAccountCurrencyService.RIAL);
            WalletAccountEntity walletAccountEntity = findUserRialAccount(walletEntity, rialCurrencyEntity, walletAccountNumber, nationalCode);
            RrnEntity rrnEntity = rrnService.generateTraceId(nationalCode, channelEntity, requestTypeService.getRequestType(type), walletAccountNumber, price);
            log.info("finish traceId ===> {}, username ({}), nationalCode ({})", rrnEntity.getUuid(), channelEntity.getUsername(), nationalCode);
            if(type.equalsIgnoreCase(RequestTypeService.BUY)){
                walletBuyLimitationService.checkBuyGeneral(channelEntity, walletEntity, new BigDecimal(price), walletAccountEntity, walletAccountNumber);
                walletBuyLimitationService.checkBuyDailyLimitation(channelEntity, walletEntity, new BigDecimal(price), walletAccountEntity, walletAccountNumber);
                walletBuyLimitationService.checkBuyMonthlyLimitation(channelEntity, walletEntity, new BigDecimal(price), walletAccountEntity, walletAccountNumber);
            }
            return new UuidResponse(rrnEntity.getUuid());
        } catch (InternalServiceException e) {
            log.error("error in generate traceId with info ===> username ({}), nationalCode ({}) error ===> ({})", channelEntity.getUsername(), nationalCode, e.getMessage());
            throw e;
        }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public PurchaseResponse sell(SellRequestDTO sellRequestDTO) throws InternalServiceException {

        if(!sellRequestDTO.getCurrency().equalsIgnoreCase(sellRequestDTO.getCommissionCurrency())){
            log.error("commission and currency not be same!!!");
            throw new InternalServiceException("commission and currency not be same", StatusService.COMMISSION_CURRENCY_NOT_VALID, HttpStatus.OK);
        }

        // Validate and retrieve currencies
        WalletAccountCurrencyEntity currencyEntity = walletAccountCurrencyService.findCurrency(sellRequestDTO.getCurrency());
        WalletAccountCurrencyEntity rialCurrencyEntity = walletAccountCurrencyService.findCurrency(WalletAccountCurrencyService.RIAL);


        // Validate merchant and wallet accounts
        MerchantEntity merchant = findMerchant(sellRequestDTO.getMerchantId());
        WalletAccountEntity merchantCurrencyAccount = findMerchantWalletAccount(merchant, currencyEntity, sellRequestDTO.getCurrency());
        WalletAccountEntity merchantRialAccount = findMerchantWalletAccount(merchant, rialCurrencyEntity, WalletAccountCurrencyService.RIAL);

        // Validate user and wallet accounts
        WalletEntity userWallet = findUserWallet(sellRequestDTO.getNationalCode());
        WalletAccountEntity userCurrencyAccount = findUserWalletAccount(userWallet, currencyEntity, sellRequestDTO.getCurrency());
        WalletAccountEntity userRialAccount = findUserRialAccount(userWallet, rialCurrencyEntity, sellRequestDTO.getWalletAccountNumber(), sellRequestDTO.getNationalCode());

        // Validate channel commission account
        WalletAccountEntity channelCommissionAccount = findChannelCommissionAccount(sellRequestDTO.getChannel(), sellRequestDTO.getCommissionCurrency());

        return redisLockService.runAfterLock(sellRequestDTO.getWalletAccountNumber(), this.getClass(), () -> processSell(new PurchaseObjectDto(
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
    public PurchaseTrackResponse purchaseTrack(ChannelEntity channel, String uid, String channelIp, String type) throws InternalServiceException {
        RrnEntity rrnEntity = rrnService.checkRrn(uid, channel, requestTypeService.getRequestType(type), "", "");
        PurchaseRequestEntity purchaseRequestEntity = requestService.findPurchaseRequestByRrnId(rrnEntity.getId());
        return helper.fillPurchaseTrackResponse(purchaseRequestEntity, statusService);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public PurchaseResponse buy(BuyRequestDTO buyRequestDTO) throws InternalServiceException {

        // Validate and retrieve currencies
        WalletAccountCurrencyEntity currencyEntity = walletAccountCurrencyService.findCurrency(buyRequestDTO.getCurrency());
        WalletAccountCurrencyEntity rialCurrencyEntity = walletAccountCurrencyService.findCurrency(WalletAccountCurrencyService.RIAL);


        // Validate merchant and wallet accounts
        MerchantEntity merchant = findMerchant(buyRequestDTO.getMerchantId());
        WalletAccountEntity merchantCurrencyAccount = findMerchantWalletAccount(merchant, currencyEntity, buyRequestDTO.getCurrency());
        WalletAccountEntity merchantRialAccount = findMerchantWalletAccount(merchant, rialCurrencyEntity, WalletAccountCurrencyService.RIAL);

        // Validate user and wallet accounts
        WalletEntity userWallet = findUserWallet(buyRequestDTO.getNationalCode());
        WalletAccountEntity userCurrencyAccount = findUserWalletAccount(userWallet, currencyEntity, buyRequestDTO.getCurrency());
        WalletAccountEntity userRialAccount = findUserRialAccount(userWallet, rialCurrencyEntity, buyRequestDTO.getWalletAccountNumber(), buyRequestDTO.getNationalCode());

        // Validate channel commission account
        WalletAccountEntity channelCommissionAccount = findChannelCommissionAccount(buyRequestDTO.getChannel(), WalletAccountCurrencyService.RIAL);

        walletBuyLimitationService.checkBuyGeneral(buyRequestDTO.getChannel(), userRialAccount.getWalletEntity(), new BigDecimal(buyRequestDTO.getPrice()), userRialAccount, buyRequestDTO.getUniqueIdentifier());
        walletBuyLimitationService.checkBuyDailyLimitation(buyRequestDTO.getChannel(), userRialAccount.getWalletEntity(), new BigDecimal(buyRequestDTO.getPrice()), userRialAccount, buyRequestDTO.getUniqueIdentifier());
        walletBuyLimitationService.checkBuyMonthlyLimitation(buyRequestDTO.getChannel(), userRialAccount.getWalletEntity(), new BigDecimal(buyRequestDTO.getPrice()), userRialAccount, buyRequestDTO.getUniqueIdentifier());

        return redisLockService.runAfterLock(buyRequestDTO.getWalletAccountNumber(), this.getClass(), () -> processBuy(new PurchaseObjectDto(
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
        ), buyRequestDTO.getUniqueIdentifier());
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
            MerchantEntity merchant, WalletAccountCurrencyEntity currencyEntity, String currency) throws InternalServiceException {
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

    private WalletAccountEntity findChannelCommissionAccount(ChannelEntity channel, String walletAccountTypeName) throws InternalServiceException {
        List<WalletAccountEntity> accounts = walletAccountService.findByWallet(channel.getWalletEntity());
        if(accounts.isEmpty()){
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

    private PurchaseResponse processBuy(
            PurchaseObjectDto purchaseObjectDto
    ) throws InternalServiceException {
        log.info("Starting purchase for uniqueIdentifier {}, nationalCode {}", purchaseObjectDto.getUniqueIdentifier(), purchaseObjectDto.getNationalCode());

        // Validate transaction
        RrnEntity rrn = validateTransaction(purchaseObjectDto.getChannel(), purchaseObjectDto.getUniqueIdentifier(), requestTypeService.getRequestType(RequestTypeService.BUY), purchaseObjectDto.getPrice(), purchaseObjectDto.getUserRialAccount().getAccountNumber());

        // Create purchase request
        PurchaseRequestEntity purchaseRequest = createPurchaseRequest(
                purchaseObjectDto, requestTypeService.getRequestType(RequestTypeService.BUY), rrn, TransactionTypeEnum.BUY);

        // Process transactions
        buyProcessTransactions(
                purchaseRequest, purchaseObjectDto.getUserRialAccount(), purchaseObjectDto.getUserCurrencyAccount(),
                purchaseObjectDto.getMerchantRialAccount(), purchaseObjectDto.getMerchantCurrencyAccount(), purchaseObjectDto.getChannelCommissionAccount(), purchaseObjectDto.getCommission());

        log.info("start updateBuyLimitation for uniqueIdentifier ({}), walletAccountId ({})", purchaseRequest.getRrnEntity().getUuid(), purchaseObjectDto.getUserRialAccount().getId());
        walletBuyLimitationService.updateBuyLimitation(purchaseObjectDto.getUserRialAccount(), purchaseObjectDto.getPrice(), purchaseObjectDto.getUniqueIdentifier());

        // Finalize purchase
        purchaseRequest.setResult(StatusService.SUCCESSFUL);
        log.info("Purchase completed successfully for uniqueIdentifier {}", purchaseObjectDto.getUniqueIdentifier());
        requestService.save(purchaseRequest);

        return helper.fillPurchaseResponse(purchaseRequest);
    }

    private PurchaseResponse processSell(
            PurchaseObjectDto purchaseObjectDto
    ) throws InternalServiceException {
        log.info("Starting sell for uniqueIdentifier {}, nationalCode {}", purchaseObjectDto.getUniqueIdentifier(), purchaseObjectDto.getNationalCode());

        // Validate transaction
        RrnEntity rrn = validateTransaction(purchaseObjectDto.getChannel(), purchaseObjectDto.getUniqueIdentifier(), requestTypeService.getRequestType(RequestTypeService.SELL), purchaseObjectDto.getPrice(), purchaseObjectDto.getUserCurrencyAccount().getAccountNumber());

        // Create purchase request
        PurchaseRequestEntity purchaseRequest = createPurchaseRequest(
                purchaseObjectDto, requestTypeService.getRequestType(RequestTypeService.SELL), rrn, TransactionTypeEnum.SELL);

        // Process transactions
        sellProcessTransactions(
                purchaseRequest, purchaseObjectDto.getUserRialAccount(), purchaseObjectDto.getUserCurrencyAccount(),
                purchaseObjectDto.getMerchantRialAccount(), purchaseObjectDto.getMerchantCurrencyAccount(), purchaseObjectDto.getChannelCommissionAccount(),
                purchaseObjectDto.getCommission());

        // Finalize purchase
        purchaseRequest.setResult(StatusService.SUCCESSFUL);
        log.info("sell completed successfully for uniqueIdentifier {}", purchaseObjectDto.getUniqueIdentifier());
        requestService.save(purchaseRequest);
        return helper.fillPurchaseResponse(purchaseRequest);
    }

    private RrnEntity validateTransaction(
            ChannelEntity channel, String uniqueIdentifier, RequestTypeEntity requestTypeEntity, BigDecimal amount, String accountNumber) throws InternalServiceException {
        log.info("Checking uniqueIdentifier {}", uniqueIdentifier);
        RrnEntity rrn = rrnService.checkRrn(uniqueIdentifier, channel, requestTypeEntity, String.valueOf(amount), accountNumber);
        log.info("Checking traceId {}", rrn.getId());
        requestService.checkTraceIdIsUnique(rrn.getId(), new PurchaseRequestEntity());
        return rrn;
    }

    private PurchaseRequestEntity createPurchaseRequest(
            PurchaseObjectDto purchaseObjectDto, RequestTypeEntity requestTypeEntity, RrnEntity rrnEntity, TransactionTypeEnum transactionTypeEnum

    ) throws InternalServiceException {
        PurchaseRequestEntity request = new PurchaseRequestEntity();
        request.setCreatedAt(new Date());
        request.setCreatedBy(purchaseObjectDto.getChannel().getUsername());
        request.setPrice(purchaseObjectDto.getPrice().longValue());
        if(TransactionTypeEnum.BUY.equals(transactionTypeEnum)) {
            request.setTerminalAmount(purchaseObjectDto.getPrice().subtract(purchaseObjectDto.getCommission()));
        }
        if(TransactionTypeEnum.SELL.equals(transactionTypeEnum)) {
            request.setTerminalAmount(purchaseObjectDto.getAmount().subtract(purchaseObjectDto.getCommission()));
        }
        request.setQuantity(purchaseObjectDto.getAmount());
        request.setWalletAccount(purchaseObjectDto.getUserRialAccount());
        request.setMerchantEntity(purchaseObjectDto.getMerchant());
        request.setNationalCode(purchaseObjectDto.getNationalCode());
        request.setRrnEntity(rrnEntity);
        request.setChannel(purchaseObjectDto.getChannel());
        request.setAdditionalData(purchaseObjectDto.getAdditionalData());
        request.setRequestTypeEntity(requestTypeEntity);
        request.setCommission(purchaseObjectDto.getCommission());
        request.setTransactionTypeEnum(transactionTypeEnum);
        requestService.save(request);
        return request;
    }

    private void buyProcessTransactions(
            PurchaseRequestEntity purchaseRequest, WalletAccountEntity userRialAccount,
            WalletAccountEntity userCurrencyAccount, WalletAccountEntity merchantRialAccount,
            WalletAccountEntity merchantCurrencyAccount, WalletAccountEntity channelCommissionAccount
            , BigDecimal commission
    ) throws InternalServiceException {

        log.info("start buyProcessTransactions for uniqueIdentifier ({})", purchaseRequest.getRrnEntity().getUuid());

        String depositTemplate = templateService.getTemplate(TemplateService.PURCHASE_DEPOSIT);
        String withdrawalTemplate = templateService.getTemplate(TemplateService.PURCHASE_WITHDRAWAL);

        Map<String, Object> model = new HashMap<>();
        model.put("traceId", String.valueOf(purchaseRequest.getRrnEntity().getId()));
        model.put("additionalData", purchaseRequest.getAdditionalData());
        model.put("amount", purchaseRequest.getQuantity());
        model.put("price", purchaseRequest.getPrice());
        model.put("merchant", purchaseRequest.getMerchantEntity().getName());
        model.put("nationalCode", purchaseRequest.getNationalCode());

        // User withdrawal (rial)
        log.info("start buy transaction for uniqueIdentifier ({}), price ({}) for withdrawal user from nationalCode ({}), walletAccountId ({})", purchaseRequest.getRrnEntity().getUuid(), purchaseRequest.getPrice(), purchaseRequest.getNationalCode(), userRialAccount.getId());
        TransactionEntity userWithdrawalTransaction = createTransaction(userRialAccount, BigDecimal.valueOf(purchaseRequest.getPrice()),
                messageResolverService.resolve(withdrawalTemplate, model), purchaseRequest.getAdditionalData(), purchaseRequest, purchaseRequest.getRrnEntity());
        transactionService.insertWithdraw(userWithdrawalTransaction);
        log.info("finish buy transaction for uniqueIdentifier ({}), price ({}) for withdrawal user from nationalCode ({}) with transactionId ({})", purchaseRequest.getRrnEntity().getUuid(), purchaseRequest.getPrice(), purchaseRequest.getNationalCode(), userWithdrawalTransaction.getId());

        // Channel commission deposit (if applicable)

        if (commission.compareTo(BigDecimal.valueOf(0L)) > 0) {
            log.info("start buy transaction for uniqueIdentifier ({}), commission ({}) for deposit commission from nationalCode ({}), walletAccountId ({})", purchaseRequest.getRrnEntity().getUuid(), commission, purchaseRequest.getNationalCode(), channelCommissionAccount.getId());
            TransactionEntity commissionDeposit = createTransaction(channelCommissionAccount, commission,
                    messageResolverService.resolve(depositTemplate, model),  purchaseRequest.getAdditionalData(), purchaseRequest, purchaseRequest.getRrnEntity());
            transactionService.insertDeposit(commissionDeposit);
            log.info("finish buy transaction for uniqueIdentifier ({}), commission ({}) for deposit commission from nationalCode ({}) with transactionId ({})", purchaseRequest.getRrnEntity().getId(), commission, purchaseRequest.getNationalCode(), commissionDeposit.getId());
        }

        // Merchant deposit (rial)
        BigDecimal merchantDepositPrice = BigDecimal.valueOf((purchaseRequest.getPrice())).subtract(commission);
        log.info("start buy transaction for uniqueIdentifier ({}), price ({}), commission ({}), finalPrice ({}) for deposit merchant walletAccountId({})", purchaseRequest.getRrnEntity().getUuid(), purchaseRequest.getPrice(), commission, merchantDepositPrice, merchantRialAccount.getId());
        TransactionEntity merchantDeposit = createTransaction(
                merchantRialAccount, merchantDepositPrice,
                messageResolverService.resolve(depositTemplate, model),  purchaseRequest.getAdditionalData(), purchaseRequest, purchaseRequest.getRrnEntity());
        transactionService.insertDeposit(merchantDeposit);
        log.info("finish buy transaction for uniqueIdentifier ({}), price ({}) for deposit merchant walletAccountId({}) with transactionId({})", purchaseRequest.getRrnEntity().getUuid(), merchantDepositPrice, merchantRialAccount.getId(), merchantDeposit.getId());

        // Merchant withdrawal (currency)
        log.info("start buy transaction for uniqueIdentifier ({}), quantity ({}) for withdrawal merchant walletAccountId({})", purchaseRequest.getRrnEntity().getUuid(), purchaseRequest.getQuantity(), merchantCurrencyAccount.getId());
        TransactionEntity merchantWithdrawal = createTransaction(
                merchantCurrencyAccount, (purchaseRequest.getQuantity()),
                messageResolverService.resolve(withdrawalTemplate, model),
                purchaseRequest.getAdditionalData(), purchaseRequest, purchaseRequest.getRrnEntity()
        );
        transactionService.insertWithdraw(merchantWithdrawal);
        log.info("finish buy transaction for uniqueIdentifier ({}), quantity ({}) for withdrawal merchant walletAccountId({}), transactionId({})", purchaseRequest.getRrnEntity().getUuid(), purchaseRequest.getQuantity(), merchantCurrencyAccount.getId(), merchantWithdrawal.getId());

        // User deposit (currency)
        log.info("start buy transaction for uniqueIdentifier ({}), quantity ({}) for deposit currency user walletAccountId({})", purchaseRequest.getRrnEntity().getUuid(), purchaseRequest.getQuantity(), userCurrencyAccount.getId());
        TransactionEntity userDepositTransaction = createTransaction(
                userCurrencyAccount,
                purchaseRequest.getQuantity(), messageResolverService.resolve(depositTemplate, model), purchaseRequest.getAdditionalData(), purchaseRequest, purchaseRequest.getRrnEntity()
        );
        transactionService.insertDeposit(userDepositTransaction);
        log.info("finish buy transaction for uniqueIdentifier ({}), quantity ({}) for deposit currency user walletAccountId({}), transactionId ({})", purchaseRequest.getRrnEntity().getUuid(), purchaseRequest.getQuantity(), userCurrencyAccount.getId(), userDepositTransaction.getId());

        log.info("terminate buyProcessTransactions for uniqueIdentifier ({})", purchaseRequest.getRrnEntity().getUuid());
    }

    private TransactionEntity createTransaction(WalletAccountEntity account, BigDecimal amount, String description,
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
            PurchaseRequestEntity purchaseRequest, WalletAccountEntity userRialAccount,
            WalletAccountEntity userCurrencyAccount, WalletAccountEntity merchantRialAccount,
            WalletAccountEntity merchantCurrencyAccount, WalletAccountEntity channelCommissionAccount
            , BigDecimal commission
    ) throws InternalServiceException {
        String depositTemplate = templateService.getTemplate(TemplateService.PURCHASE_DEPOSIT);
        String withdrawalTemplate = templateService.getTemplate(TemplateService.PURCHASE_WITHDRAWAL);

        Map<String, Object> model = new HashMap<>();
        model.put("traceId", String.valueOf(purchaseRequest.getRrnEntity().getId()));
        model.put("additionalData", purchaseRequest.getAdditionalData());
        model.put("amount", purchaseRequest.getQuantity());
        model.put("price", purchaseRequest.getPrice());
        model.put("merchant", purchaseRequest.getMerchantEntity().getName());
        model.put("nationalCode", purchaseRequest.getNationalCode());

        // merchant withdrawal (rial)
        TransactionEntity merchantRialWithdrawal = createTransaction(
                merchantRialAccount, BigDecimal.valueOf(purchaseRequest.getPrice()),
                messageResolverService.resolve(withdrawalTemplate, model), purchaseRequest.getAdditionalData(), purchaseRequest, purchaseRequest.getRrnEntity());
        transactionService.insertWithdraw(merchantRialWithdrawal);

        // Channel commission deposit (if applicable)
        if (commission.compareTo(BigDecimal.valueOf(0L)) > 0) {
            TransactionEntity commissionDeposit = createTransaction(channelCommissionAccount, commission,
                    messageResolverService.resolve(depositTemplate, model), purchaseRequest.getAdditionalData(), purchaseRequest, purchaseRequest.getRrnEntity());
            transactionService.insertDeposit(commissionDeposit);
        }

        // user deposit (rial)
        TransactionEntity userRialDeposit = createTransaction(userRialAccount, BigDecimal.valueOf(purchaseRequest.getPrice()).subtract(commission),
                messageResolverService.resolve(depositTemplate, model), purchaseRequest.getAdditionalData(), purchaseRequest, purchaseRequest.getRrnEntity());
        transactionService.insertDeposit(userRialDeposit);

        // merchant withdrawal (currency)
        TransactionEntity merchantCurrencyWithdrawal = createTransaction(userCurrencyAccount, purchaseRequest.getQuantity(),
                messageResolverService.resolve(withdrawalTemplate, model), purchaseRequest.getAdditionalData(), purchaseRequest, purchaseRequest.getRrnEntity());
        transactionService.insertWithdraw(merchantCurrencyWithdrawal);

        // user deposit (currency)
        TransactionEntity userCurrencyDeposit = createTransaction(merchantCurrencyAccount, purchaseRequest.getQuantity(),
                messageResolverService.resolve(depositTemplate, model), purchaseRequest.getAdditionalData(), purchaseRequest, purchaseRequest.getRrnEntity());
        transactionService.insertDeposit(userCurrencyDeposit);
    }
}
