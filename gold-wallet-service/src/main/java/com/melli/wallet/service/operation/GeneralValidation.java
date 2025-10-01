package com.melli.wallet.service.operation;

import com.melli.wallet.domain.dto.BalanceDTO;
import com.melli.wallet.exception.InternalServiceException;

import java.math.BigDecimal;

/**
 * Class Name: GeneralValidation
 * Author: Mahdi Shirinabadi
 * Date: 10/1/2025
 */
public interface GeneralValidation {

    void checkCommissionLessThanQuantity(BigDecimal commission, BigDecimal quantity) throws InternalServiceException;
    void checkBalance(BalanceDTO balanceDTO, BigDecimal amount, String walletAccountNumber) throws InternalServiceException;
}
