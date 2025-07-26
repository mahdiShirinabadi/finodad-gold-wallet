package com.melli.wallet.service.impl;

import com.melli.wallet.domain.dto.ChargeObjectDTO;
import com.melli.wallet.domain.dto.PurchaseObjectDto;
import com.melli.wallet.domain.enumaration.CashInPaymentTypeEnum;
import com.melli.wallet.domain.enumaration.TransactionTypeEnum;
import com.melli.wallet.domain.master.entity.*;
import com.melli.wallet.domain.response.purchase.PurchaseResponse;
import com.melli.wallet.exception.InternalServiceException;
import com.melli.wallet.service.*;
import com.melli.wallet.utils.Helper;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Class Name: PurchaseTransactionalService
 * Author: Mahdi Shirinabadi
 * Date: 7/21/2025
 */
@Service
@Log4j2
@RequiredArgsConstructor
public class PurchaseTransactionalService {

    private final RrnService rrnService;
    private final RequestService requestService;
    private final Helper helper;
    private final TransactionService transactionService;
    private final MessageResolverService messageResolverService;
    private final TemplateService templateService;
    private final RequestTypeService requestTypeService;
    private final WalletBuyLimitationService walletBuyLimitationService;
    private final WalletSellLimitationService walletSellLimitationService;
    private final CashInService cashInService;

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public PurchaseResponse processSell(
            PurchaseObjectDto purchaseObjectDto
    ) throws InternalServiceException {
        log.info("Starting sell for uniqueIdentifier {}, nationalCode {}", purchaseObjectDto.getUniqueIdentifier(), purchaseObjectDto.getNationalCode());

        // Validate transaction
        RrnEntity rrn = validateTransaction(purchaseObjectDto.getChannel(), purchaseObjectDto.getUniqueIdentifier(), requestTypeService.getRequestType(RequestTypeService.SELL), purchaseObjectDto.getQuantity(), purchaseObjectDto.getUserCurrencyAccount().getAccountNumber());

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

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public PurchaseResponse processBuy(
            PurchaseObjectDto purchaseObjectDto,
            String uuidForCharge,
            String refNumber,
            boolean directCharge
    ) throws InternalServiceException {

        if(directCharge){
            log.info("call from buy direct and first we charge a account for customer with nationalCode ({}), walletAccount ({})", purchaseObjectDto.getNationalCode(), purchaseObjectDto.getUserRialAccount().getAccountNumber());
            cashInService.charge(new ChargeObjectDTO(purchaseObjectDto.getChannel(), purchaseObjectDto.getNationalCode(), uuidForCharge,
                    String.valueOf(purchaseObjectDto.getPrice()), refNumber, purchaseObjectDto.getUserRialAccount().getAccountNumber(),purchaseObjectDto.getAdditionalData(),"0.0.0.0", CashInPaymentTypeEnum.ACCOUNT_TO_ACCOUNT.getText()));
        }

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
        walletBuyLimitationService.updateLimitation(purchaseObjectDto.getUserCurrencyAccount(), purchaseObjectDto.getPrice(), purchaseObjectDto.getQuantity(), purchaseObjectDto.getUniqueIdentifier());

        // Finalize purchase
        purchaseRequest.setResult(StatusService.SUCCESSFUL);
        log.info("Purchase completed successfully for uniqueIdentifier {}", purchaseObjectDto.getUniqueIdentifier());
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
        if (TransactionTypeEnum.BUY.equals(transactionTypeEnum)) {
            request.setTerminalAmount(purchaseObjectDto.getPrice().subtract(purchaseObjectDto.getCommission()));
        }
        if (TransactionTypeEnum.SELL.equals(transactionTypeEnum)) {
            request.setTerminalAmount(purchaseObjectDto.getQuantity().subtract(purchaseObjectDto.getCommission()));
        }
        request.setQuantity(purchaseObjectDto.getQuantity());
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

        String depositTemplate = templateService.getTemplate(TemplateService.BUY_DEPOSIT);
        String withdrawalTemplate = templateService.getTemplate(TemplateService.BUY_WITHDRAWAL);

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

        // commission must be rial
        if (commission.compareTo(BigDecimal.valueOf(0L)) > 0) {
            log.info("start buy transaction for uniqueIdentifier ({}), commission ({}) for deposit commission from nationalCode ({}), walletAccountId ({})", purchaseRequest.getRrnEntity().getUuid(), commission, purchaseRequest.getNationalCode(), channelCommissionAccount.getId());
            TransactionEntity commissionDeposit = createTransaction(channelCommissionAccount, commission,
                    messageResolverService.resolve(depositTemplate, model), purchaseRequest.getAdditionalData(), purchaseRequest, purchaseRequest.getRrnEntity());
            transactionService.insertDeposit(commissionDeposit);
            log.info("finish buy transaction for uniqueIdentifier ({}), commission ({}) for deposit commission from nationalCode ({}) with transactionId ({})", purchaseRequest.getRrnEntity().getId(), commission, purchaseRequest.getNationalCode(), commissionDeposit.getId());
        }

        // Merchant deposit (rial)
        BigDecimal merchantDepositPrice = BigDecimal.valueOf((purchaseRequest.getPrice())).subtract(commission);
        log.info("start buy transaction for uniqueIdentifier ({}), price ({}), commission ({}), finalPrice ({}) for deposit merchant walletAccountId({})", purchaseRequest.getRrnEntity().getUuid(), purchaseRequest.getPrice(), commission, merchantDepositPrice, merchantRialAccount.getId());
        TransactionEntity merchantDeposit = createTransaction(
                merchantRialAccount, merchantDepositPrice,
                messageResolverService.resolve(depositTemplate, model), purchaseRequest.getAdditionalData(), purchaseRequest, purchaseRequest.getRrnEntity());
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

    private void sellProcessTransactions(
            PurchaseRequestEntity purchaseRequest, WalletAccountEntity userRialAccount,
            WalletAccountEntity userCurrencyAccount, WalletAccountEntity merchantRialAccount,
            WalletAccountEntity merchantCurrencyAccount, WalletAccountEntity channelCommissionAccount
            , BigDecimal commission
    ) throws InternalServiceException {
        log.info("start sellProcessTransactions for uniqueIdentifier ({})", purchaseRequest.getRrnEntity().getUuid());
        String depositTemplate = templateService.getTemplate(TemplateService.SELL_DEPOSIT);
        String withdrawalTemplate = templateService.getTemplate(TemplateService.SELL_WITHDRAWAL);

        Map<String, Object> model = new HashMap<>();
        model.put("traceId", String.valueOf(purchaseRequest.getRrnEntity().getId()));
        model.put("additionalData", purchaseRequest.getAdditionalData());
        model.put("amount", purchaseRequest.getQuantity());
        model.put("price", purchaseRequest.getPrice());
        model.put("merchant", purchaseRequest.getMerchantEntity().getName());
        model.put("nationalCode", purchaseRequest.getNationalCode());

        // merchant withdrawal (rial)
        log.info("start sell transaction for uniqueIdentifier ({}), price ({}) for merchant withdrawal from id ({}), walletAccountId ({})", purchaseRequest.getRrnEntity().getUuid(), purchaseRequest.getPrice(), purchaseRequest.getMerchantEntity().getId(), userRialAccount.getId());

        TransactionEntity merchantRialWithdrawal = createTransaction(
                merchantRialAccount, BigDecimal.valueOf(purchaseRequest.getPrice()),
                messageResolverService.resolve(withdrawalTemplate, model), purchaseRequest.getAdditionalData(), purchaseRequest, purchaseRequest.getRrnEntity());
        transactionService.insertWithdraw(merchantRialWithdrawal);
        log.info("finish sell transaction for uniqueIdentifier ({}), price ({}) for merchant withdrawal from id ({}), walletAccountId ({}), transactionId ({})", purchaseRequest.getRrnEntity().getUuid(), purchaseRequest.getPrice(), purchaseRequest.getMerchantEntity().getId(), userRialAccount.getId(),
                merchantRialWithdrawal.getId());

        // Channel commission deposit (if applicable)
        //commission type must be currency
        if (commission.compareTo(BigDecimal.valueOf(0L)) > 0) {
            log.info("start sell transaction for uniqueIdentifier ({}), commission ({}) for deposit commission from nationalCode ({}), walletAccountId ({})", purchaseRequest.getRrnEntity().getUuid(), commission, purchaseRequest.getNationalCode(), channelCommissionAccount.getId());
            TransactionEntity commissionDeposit = createTransaction(channelCommissionAccount, commission,
                    messageResolverService.resolve(depositTemplate, model), purchaseRequest.getAdditionalData(), purchaseRequest, purchaseRequest.getRrnEntity());
            transactionService.insertDeposit(commissionDeposit);
            log.info("finish sell transaction for uniqueIdentifier ({}), commission ({}) for deposit commission from nationalCode ({}) with transactionId ({})", purchaseRequest.getRrnEntity().getId(), commission, purchaseRequest.getNationalCode(), commissionDeposit.getId());
        }

        // user deposit (rial) (price)
        log.info("start sell transaction for uniqueIdentifier ({}), price ({}) for user deposit currency user walletAccountId({})", purchaseRequest.getRrnEntity().getUuid(), purchaseRequest.getPrice(), userCurrencyAccount.getId());
        TransactionEntity userRialDeposit = createTransaction(userRialAccount, BigDecimal.valueOf(purchaseRequest.getPrice()),
                messageResolverService.resolve(depositTemplate, model), purchaseRequest.getAdditionalData(), purchaseRequest, purchaseRequest.getRrnEntity());
        transactionService.insertDeposit(userRialDeposit);
        log.info("finish sell transaction for uniqueIdentifier ({}), price ({}) for user deposit currency user walletAccountId({}), transactionId ({})", purchaseRequest.getRrnEntity().getUuid(), purchaseRequest.getPrice(), userCurrencyAccount.getId(), userRialDeposit.getId());

        // user withdrawal (currency) (quantity)
        log.info("start sell transaction for uniqueIdentifier ({}), quantity ({}) for user withdrawal user walletAccountId({})", purchaseRequest.getRrnEntity().getUuid(), purchaseRequest.getQuantity(), merchantCurrencyAccount.getId());
        TransactionEntity merchantCurrencyWithdrawal = createTransaction(userCurrencyAccount, (purchaseRequest.getQuantity()),
                messageResolverService.resolve(withdrawalTemplate, model), purchaseRequest.getAdditionalData(), purchaseRequest, purchaseRequest.getRrnEntity());
        transactionService.insertWithdraw(merchantCurrencyWithdrawal);
        log.info("start sell transaction for uniqueIdentifier ({}), quantity ({}) for user withdrawal user walletAccountId({})", purchaseRequest.getRrnEntity().getUuid(), purchaseRequest.getQuantity(), merchantCurrencyAccount.getId());

        // merchant deposit (currency) (quantity - commission)
        log.info("start sell transaction for uniqueIdentifier ({}), quantity ({}) for merchant deposit currency user walletAccountId({})", purchaseRequest.getRrnEntity().getUuid(), purchaseRequest.getQuantity().subtract(commission), userCurrencyAccount.getId());
        TransactionEntity merchantCurrencyDeposit = createTransaction(merchantCurrencyAccount, purchaseRequest.getQuantity().subtract(commission),
                messageResolverService.resolve(depositTemplate, model), purchaseRequest.getAdditionalData(), purchaseRequest, purchaseRequest.getRrnEntity());
        transactionService.insertDeposit(merchantCurrencyDeposit);
        log.info("finish sell transaction for uniqueIdentifier ({}), quantity ({}) for merchant deposit currency user walletAccountId({}), transactionId ({})", purchaseRequest.getRrnEntity().getUuid(), purchaseRequest.getQuantity().subtract(commission), userCurrencyAccount.getId(), merchantCurrencyDeposit.getId());
        walletSellLimitationService.updateLimitation(userCurrencyAccount, new BigDecimal(purchaseRequest.getPrice()), purchaseRequest.getQuantity(), purchaseRequest.getRrnEntity().getUuid());
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

}
