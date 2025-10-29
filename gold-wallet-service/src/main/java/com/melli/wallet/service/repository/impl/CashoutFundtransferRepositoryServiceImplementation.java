package com.melli.wallet.service.repository.impl;

import com.melli.wallet.domain.master.entity.CashoutFundTransferEntity;
import com.melli.wallet.domain.master.persistence.CashoutFundTransferRepository;
import com.melli.wallet.exception.InternalServiceException;
import com.melli.wallet.service.repository.CashoutFundtransferRepositoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

/**
 * Class Name: CashoutFundtransferRepositoryServiceImplementation
 * Author: Mahdi Shirinabadi
 * Date: 10/29/2025
 */
@Service
@Log4j2
@RequiredArgsConstructor
public class CashoutFundtransferRepositoryServiceImplementation implements CashoutFundtransferRepositoryService {

    private final CashoutFundTransferRepository cashoutFundtransferRepository;

    @Override
    public void save(CashoutFundTransferEntity cashoutFundTransferEntity) throws InternalServiceException {
        cashoutFundtransferRepository.save(cashoutFundTransferEntity);
    }
}
