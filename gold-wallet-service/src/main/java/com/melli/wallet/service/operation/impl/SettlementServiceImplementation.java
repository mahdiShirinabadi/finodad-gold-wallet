package com.melli.wallet.service.operation.impl;

import com.melli.wallet.Constant;
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
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
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
    private final RedisLockService redisLockService;

    @Override
//    @Async
    public void settlement(CashOutRequestEntity cashOutRequestEntityInput) throws InternalServiceException {

        String key = String.valueOf(cashOutRequestEntityInput.getId());

        redisLockService.runAfterLock(key, this.getClass(), ()->{

            CashOutRequestEntity cashOutRequestEntity = requestRepositoryService.findCashOutById(cashOutRequestEntityInput.getId());
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
            fundTransferAccountToAccountRequestEntity.setTraceNumber(UUID.randomUUID().toString());
            fundTransferAccountToAccountRequestEntity.setRequestTypeEntity(
                    requestTypeRepositoryService.getRequestType(RequestTypeRepositoryService.SETTLEMENT));
            fundTransferAccountToAccountRequestEntity.setCreatedAt(new Date());
            fundTransferAccountToAccountRequestEntity.setCreatedBy(cashOutRequestEntity.getChannel().getUsername());

            requestRepositoryService.save(fundTransferAccountToAccountRequestEntity);

            Long balance = sadadOperationService.getBalance(fundTransferAccountToAccountRequestEntity.getFromAccount());
            if (balance <= fundTransferAccountToAccountRequestEntity.getAmount()) {
                cashOutRequestEntity.setSettlementStepEnum(SettlementStepEnum.INITIAL);
                requestRepositoryService.save(cashOutRequestEntity);
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
            return null;
        }, key);
    }

    @Override
    public void settlementById(long id) throws InternalServiceException {
        CashOutRequestEntity cashOutRequestEntity = requestRepositoryService.findCashOutById(id);
        if(cashOutRequestEntity == null) {
            log.error("cash out request entity is null for id ({})", id);
            throw new InternalServiceException("request not found", StatusRepositoryService.RECORD_NOT_FOUND, HttpStatus.OK);
        }
        settlement(cashOutRequestEntity);
    }

    @SchedulerLock(
            name = Constant.BATCH_SETTLEMENT_JOB_LOCK,
            lockAtLeastFor = "PT600S", // lock for at least 10 minutes
            lockAtMostFor = "PT3600S" // lock for at most 1 hour
    )
    @Override
    public void bachSettlement(List<CashOutRequestEntity> cashOutRequestEntityList) throws InternalServiceException {

        if (cashOutRequestEntityList == null || cashOutRequestEntityList.isEmpty()) {
            log.error("cashOutRequestEntityList is null or empty");
            throw new InternalServiceException("cashOutRequestEntityList is null or empty", StatusRepositoryService.GENERAL_ERROR, HttpStatus.OK);
        }

        for(CashOutRequestEntity cashOutRequestEntity : cashOutRequestEntityList) {
            try {
                settlement(cashOutRequestEntity);
            } catch (InternalServiceException e) {
                log.error("Settlement failed for entity id ({}), stopping batch processing. Error: {}", 
                        cashOutRequestEntity.getId(), e.getMessage());
                break; // Stop processing remaining items when an error occurs
            }
        }
    }
}
