package com.melli.wallet.service.operation.impl;

import com.melli.wallet.domain.dto.BalanceDTO;
import com.melli.wallet.domain.enumaration.SettlementStepEnum;
import com.melli.wallet.domain.master.entity.CashOutRequestEntity;
import com.melli.wallet.domain.master.entity.CashoutFundTransferEntity;
import com.melli.wallet.domain.master.entity.FundTransferAccountToAccountRequestEntity;
import com.melli.wallet.domain.master.entity.WalletAccountEntity;
import com.melli.wallet.domain.response.cash.CashOutResponse;
import com.melli.wallet.domain.slave.persistence.ReportPhysicalCashOutRequestRepository;
import com.melli.wallet.exception.InternalServiceException;
import com.melli.wallet.service.operation.*;
import com.melli.wallet.service.repository.*;
import com.melli.wallet.utils.Helper;
import com.melli.wallet.utils.RedisLockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Class Name: SettlementServiceImplementation
 * Author: Mahdi Shirinabadi
 * Date: 10/29/2025
 */
@Service
@Log4j2
@RequiredArgsConstructor
public class SettlementServiceImplementation implements SettlementService {

    private final RequestRepositoryService requestRepositoryService;
    private final RequestTypeRepositoryService requestTypeRepositoryService;
    private final SadadOperationService sadadOperationService;
    private final CashoutFundtransferRepositoryService cashoutFundtransferRepositoryService;

    @Override
//    @Async
    public void settlement(CashOutRequestEntity cashOutRequestEntity) throws InternalServiceException {

        // Check if already settled
        if (!cashOutRequestEntity.getSettlementStepEnum().equals(SettlementStepEnum.INITIAL)) {
            log.error("Cash out request already settled for uuid: {}", cashOutRequestEntity.getRrnEntity() != null ? cashOutRequestEntity.getRrnEntity().getUuid() : "unknown");
            throw new InternalServiceException("Cash out request is already settled", StatusRepositoryService.GENERAL_ERROR, HttpStatus.OK);
        }

        cashOutRequestEntity.setSettlementStepEnum(SettlementStepEnum.IN_PROCESS);
        requestRepositoryService.save(cashOutRequestEntity);

        WalletAccountEntity walletAccountEntity = cashOutRequestEntity.getWalletAccountEntity();

        log.info("start create fundTransfer from account ({}) to account ({})", cashOutRequestEntity.getMerchantEntity().getBankAccountNumber(), cashOutRequestEntity.getIban());
        FundTransferAccountToAccountRequestEntity fundTransferAccountToAccountRequestEntity =
                new FundTransferAccountToAccountRequestEntity();
        fundTransferAccountToAccountRequestEntity.setMultiTransaction(false);
        fundTransferAccountToAccountRequestEntity.setRrnEntity(cashOutRequestEntity.getRrnEntity());
        fundTransferAccountToAccountRequestEntity.setFromAccount(cashOutRequestEntity.getMerchantEntity().getBankAccountNumber());
        fundTransferAccountToAccountRequestEntity.setToAccount(cashOutRequestEntity.getIban());
        fundTransferAccountToAccountRequestEntity.setAmount(cashOutRequestEntity.getAmount());
        fundTransferAccountToAccountRequestEntity.setNationalCode(walletAccountEntity.getWalletEntity().getNationalCode());
        fundTransferAccountToAccountRequestEntity.setAdditionalData(cashOutRequestEntity.getAdditionalData());
        fundTransferAccountToAccountRequestEntity.setChannel(cashOutRequestEntity.getChannel());
        fundTransferAccountToAccountRequestEntity.setChannelIp(cashOutRequestEntity.getChannelIp());
        fundTransferAccountToAccountRequestEntity.setRequestTypeEntity(
                requestTypeRepositoryService.getRequestType(RequestTypeRepositoryService.SETTLEMENT));
        fundTransferAccountToAccountRequestEntity.setCreatedAt(new Date());
        fundTransferAccountToAccountRequestEntity.setCreatedBy(cashOutRequestEntity.getChannel().getUsername());

        requestRepositoryService.save(fundTransferAccountToAccountRequestEntity);

        Long balance = sadadOperationService.getBalance(fundTransferAccountToAccountRequestEntity.getFromAccount());
        if (balance < fundTransferAccountToAccountRequestEntity.getAmount()) {
            log.error("balance for account ({}) is: ({}) and less than ({}) ", fundTransferAccountToAccountRequestEntity.getFromAccount(), balance,
                    fundTransferAccountToAccountRequestEntity.getAmount());
            throw new InternalServiceException("balance is not enough", StatusRepositoryService.BANK_ACCOUNT_BALANCE_NOT_ENOUGH, HttpStatus.OK);
        }

        try {
            sadadOperationService.accountToAccount(fundTransferAccountToAccountRequestEntity);
            cashOutRequestEntity.setSettlementStepEnum(SettlementStepEnum.SUCCESS);
            requestRepositoryService.save(cashOutRequestEntity);
        } catch (InternalServiceException exception) {
            log.error("failed in accountToAccount in sadad exception({}), uuid ({})",
                    exception.getMessage(), fundTransferAccountToAccountRequestEntity.getRrnEntity().getUuid());
            throw exception;
        } finally {
            requestRepositoryService.save(fundTransferAccountToAccountRequestEntity);
        }

        CashoutFundTransferEntity cashoutFundTransferEntity = new CashoutFundTransferEntity();
        cashoutFundTransferEntity.setCreatedAt(new Date());
        cashoutFundTransferEntity.setCreatedBy(cashOutRequestEntity.getChannel().getUsername());
        cashoutFundTransferEntity.setCashoutRequestEntity(cashOutRequestEntity);
        cashoutFundTransferEntity.setFundTransferAccountToAccountRequestEntity(fundTransferAccountToAccountRequestEntity);
        cashoutFundtransferRepositoryService.save(cashoutFundTransferEntity);
        log.info("=== CASH OUT WITHDRAWAL OPERATION COMPLETED SUCCESSFULLY ===");
    }

    @Override
    public void bachSettlement(List<CashOutRequestEntity> cashOutRequestEntityList) throws InternalServiceException {

        if (cashOutRequestEntityList == null || cashOutRequestEntityList.isEmpty()) {
            log.error("cashOutRequestEntityList is null or empty");
            throw new InternalServiceException("cashOutRequestEntityList is null or empty", StatusRepositoryService.GENERAL_ERROR, HttpStatus.OK);
        }

        // Check if any cash out request is already settled
        for (CashOutRequestEntity entity : cashOutRequestEntityList) {
            if (!entity.getSettlementStepEnum().equals(SettlementStepEnum.INITIAL)) {
                String uuid = entity.getRrnEntity() != null ? entity.getRrnEntity().getUuid() : "unknown";
                log.error("Cash out with id ({}) request dont step initial for uuid: {}", entity.getId(), uuid);
                throw new InternalServiceException("Cash out request is already settled: " + uuid, StatusRepositoryService.GENERAL_ERROR, HttpStatus.OK);
            }
        }

        // Use first item for reference values
        CashOutRequestEntity firstItem = cashOutRequestEntityList.getFirst();
        String firstIban = firstItem.getIban();
        String firstChannelUsername = firstItem.getChannel() != null ? firstItem.getChannel().getUsername() : null;
        String firstFromAccount = firstItem.getMerchantEntity() != null ? firstItem.getMerchantEntity().getBankAccountNumber() : null;
        String firstChannelIp = firstItem.getChannelIp();

        // Validate all IBANs are the same
        for (CashOutRequestEntity entity : cashOutRequestEntityList) {
            if (entity.getIban() == null || !entity.getIban().equals(firstIban)) {
                log.error("IBAN mismatch in batch settlement. First IBAN: {}, Found IBAN: {}", firstIban, entity.getIban());
                throw new InternalServiceException("All IBANs must be the same in batch settlement", StatusRepositoryService.GENERAL_ERROR, HttpStatus.OK);
            }
            // Validate all channels are the same
            String currentChannelUsername = entity.getChannel() != null ? entity.getChannel().getUsername() : null;
            if ((firstChannelUsername == null) != (currentChannelUsername == null) ||
                    (firstChannelUsername != null && !firstChannelUsername.equals(currentChannelUsername))) {
                log.error("Channel mismatch in batch settlement. First Channel: {}, Found Channel: {}", firstChannelUsername, currentChannelUsername);
                throw new InternalServiceException("All channels must be the same in batch settlement", StatusRepositoryService.GENERAL_ERROR, HttpStatus.OK);
            }
        }

        // change all to in_process
        for (CashOutRequestEntity entity : cashOutRequestEntityList) {
            entity.setSettlementStepEnum(SettlementStepEnum.IN_PROCESS);
            requestRepositoryService.save(entity);
        }

        // Sum all amounts
        Long totalAmount = 0L;
        for (CashOutRequestEntity entity : cashOutRequestEntityList) {
            totalAmount += entity.getAmount();
        }

        log.info("start batch settlement for {} items, total amount: {}, IBAN: {}", cashOutRequestEntityList.size(), totalAmount, firstIban);

        // Use first item's wallet account for national code
        WalletAccountEntity walletAccountEntity = firstItem.getWalletAccountEntity();

        // Create single FundTransferAccountToAccountRequestEntity for batch
        FundTransferAccountToAccountRequestEntity fundTransferAccountToAccountRequestEntity =
                new FundTransferAccountToAccountRequestEntity();
        // Use first item's RRN as primary reference
        fundTransferAccountToAccountRequestEntity.setRrnEntity(firstItem.getRrnEntity());
        fundTransferAccountToAccountRequestEntity.setFromAccount(firstFromAccount);
        fundTransferAccountToAccountRequestEntity.setToAccount(firstIban);
        fundTransferAccountToAccountRequestEntity.setAmount(totalAmount);
        fundTransferAccountToAccountRequestEntity.setNationalCode(walletAccountEntity.getWalletEntity().getNationalCode());
        fundTransferAccountToAccountRequestEntity.setAdditionalData(firstItem.getAdditionalData());
        fundTransferAccountToAccountRequestEntity.setChannel(firstItem.getChannel());
        fundTransferAccountToAccountRequestEntity.setChannelIp(firstChannelIp);
        fundTransferAccountToAccountRequestEntity.setRequestTypeEntity(
                requestTypeRepositoryService.getRequestType(RequestTypeRepositoryService.SETTLEMENT));
        fundTransferAccountToAccountRequestEntity.setCreatedAt(new Date());
        fundTransferAccountToAccountRequestEntity.setCreatedBy(firstItem.getChannel().getUsername());

        requestRepositoryService.save(fundTransferAccountToAccountRequestEntity);

        // Check balance
        Long balance = sadadOperationService.getBalance(fundTransferAccountToAccountRequestEntity.getFromAccount());
        if (balance < fundTransferAccountToAccountRequestEntity.getAmount()) {
            log.error("balance for account ({}) is: ({}) and less than ({}) ", fundTransferAccountToAccountRequestEntity.getFromAccount(), balance,
                    fundTransferAccountToAccountRequestEntity.getAmount());
            throw new InternalServiceException("balance is not enough", StatusRepositoryService.BANK_ACCOUNT_BALANCE_NOT_ENOUGH, HttpStatus.OK);
        }

        try {
            sadadOperationService.accountToAccount(fundTransferAccountToAccountRequestEntity);
            // Mark all cash out requests as settled
            for (CashOutRequestEntity entity : cashOutRequestEntityList) {
                entity.setSettlementStepEnum(SettlementStepEnum.SUCCESS);
                requestRepositoryService.save(entity);
            }
        } catch (InternalServiceException exception) {
            log.error("failed in accountToAccount in sadad for batch settlement, exception({}), uuid ({})",
                    exception.getMessage(), fundTransferAccountToAccountRequestEntity.getRrnEntity().getUuid());
            throw exception;
        } finally {
            requestRepositoryService.save(fundTransferAccountToAccountRequestEntity);
        }

        // Link all cash out requests to the single fund transfer
        for (CashOutRequestEntity entity : cashOutRequestEntityList) {
            CashoutFundTransferEntity cashoutFundTransferEntity = new CashoutFundTransferEntity();
            cashoutFundTransferEntity.setCreatedAt(new Date());
            cashoutFundTransferEntity.setCreatedBy(entity.getChannel().getUsername());
            cashoutFundTransferEntity.setCashoutRequestEntity(entity);
            cashoutFundTransferEntity.setFundTransferAccountToAccountRequestEntity(fundTransferAccountToAccountRequestEntity);
            cashoutFundtransferRepositoryService.save(cashoutFundTransferEntity);
        }

        log.info("=== BATCH CASH OUT SETTLEMENT OPERATION COMPLETED SUCCESSFULLY ===");
    }
}
