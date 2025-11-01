package com.melli.wallet.service.operation;

import com.melli.wallet.domain.master.entity.CashOutRequestEntity;
import com.melli.wallet.exception.InternalServiceException;

import java.util.List;

/**
 * Class Name: SettlementService
 * Author: Mahdi Shirinabadi
 * Date: 10/29/2025
 */
public interface SettlementService {

    void settlement(CashOutRequestEntity cashOutRequestEntity) throws InternalServiceException;

    void settlementById(long id) throws InternalServiceException;

    void bachSettlement(List<CashOutRequestEntity> cashOutRequestEntity) throws InternalServiceException;
}
