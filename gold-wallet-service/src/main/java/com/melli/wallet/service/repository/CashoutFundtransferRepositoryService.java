package com.melli.wallet.service.repository;

import com.melli.wallet.domain.enumaration.GiftCardStepStatus;
import com.melli.wallet.domain.enumaration.SettlementStepEnum;
import com.melli.wallet.domain.master.entity.CashOutRequestEntity;
import com.melli.wallet.domain.master.entity.CashoutFundTransferEntity;
import com.melli.wallet.domain.master.entity.GiftCardEntity;
import com.melli.wallet.domain.slave.entity.ReportCashOutRequestEntity;
import com.melli.wallet.domain.slave.entity.ReportCashoutFundTransferEntity;
import com.melli.wallet.exception.InternalServiceException;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Class Name: GiftCardOperationService
 * Author: Mahdi Shirinabadi
 * Date: 9/22/2025
 */
public interface CashoutFundtransferRepositoryService {
    void save(CashoutFundTransferEntity cashoutFundTransferEntity) throws InternalServiceException;

    List<ReportCashoutFundTransferEntity> findAllByCashout(ReportCashOutRequestEntity cashOutRequestEntity);
}
