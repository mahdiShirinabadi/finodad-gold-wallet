package com.melli.wallet.service;

import com.melli.wallet.domain.master.entity.TransactionEntity;
import com.melli.wallet.exception.InternalServiceException;

import java.util.List;

public interface TransactionService {

	List<TransactionEntity> walletLastTransaction(long walletAccountId, int limit);

	void insertDeposit(TransactionEntity transaction) throws InternalServiceException;

	void insertWithdraw(TransactionEntity transaction) throws InternalServiceException;
}
