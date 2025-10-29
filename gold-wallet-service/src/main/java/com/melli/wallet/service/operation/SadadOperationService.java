package com.melli.wallet.service.operation;

import com.melli.wallet.domain.master.entity.CashOutRequestEntity;
import com.melli.wallet.domain.master.entity.FundTransferAccountToAccountRequestEntity;
import com.melli.wallet.domain.master.entity.ShahkarInfoEntity;
import com.melli.wallet.exception.InternalServiceException;

public interface SadadOperationService {

    Boolean shahkar(ShahkarInfoEntity shahkarInfoEntity) throws InternalServiceException;

    void sendSms(String message, String mobile) throws InternalServiceException;

    String accountToAccount(FundTransferAccountToAccountRequestEntity fundTransferAccountToAccountRequestEntity) throws InternalServiceException;

    String inquiryAccountToAccount(String uuid, Long amount) throws InternalServiceException;

    String statement(String amount, String srcAccountNumber, Long timeStampTransactionDate, String traceNumber, int timeFrame, int page, int length) throws InternalServiceException;

    Long getBalance(String accountNumber) throws InternalServiceException;


}
