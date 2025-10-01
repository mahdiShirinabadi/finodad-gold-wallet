package com.melli.wallet.service.operation.impl;

import com.melli.wallet.domain.dto.BalanceDTO;
import com.melli.wallet.exception.InternalServiceException;
import com.melli.wallet.service.operation.GeneralValidation;
import com.melli.wallet.service.repository.StatusRepositoryService;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * Class Name: GeneralValidationImplementation
 * Author: Mahdi Shirinabadi
 * Date: 10/1/2025
 */
@Log4j2
@Service
public class GeneralValidationImplementation implements GeneralValidation {

    public static final BigDecimal MINIMUM_BALANCE_THRESHOLD = BigDecimal.ZERO;
    public static final int COMPARE_NUMBER = 0;

    @Override
    public void checkCommissionLessThanQuantity(BigDecimal commission, BigDecimal quantity) throws InternalServiceException {
        if ((quantity.subtract(commission)).compareTo(BigDecimal.ZERO) <= COMPARE_NUMBER) {
            log.error("commission ({}) is bigger than quantity ({})", commission, quantity);
            throw new InternalServiceException("commission is bigger than quantity", StatusRepositoryService.COMMISSION_BIGGER_THAN_QUANTITY, HttpStatus.OK);
        }
    }

    public void checkBalance(BalanceDTO balanceDTO, BigDecimal amount, String walletAccountNumber) throws InternalServiceException {
        if (balanceDTO.getAvailableBalance().compareTo(amount) <= 0) {
            log.error("balance for account ({}) is ({}) and not enough for quantity ({})", walletAccountNumber, balanceDTO.getAvailableBalance(), amount);
            throw new InternalServiceException("balance not enough", StatusRepositoryService.INSUFFICIENT_BALANCE, HttpStatus.OK);
        }
    }
}
