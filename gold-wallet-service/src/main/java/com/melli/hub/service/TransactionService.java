package com.melli.hub.service;

import com.melli.hub.domain.master.entity.TransactionEntity;
import com.melli.hub.exception.InternalServiceException;

import java.util.List;

public interface TransactionService {

	List<TransactionEntity> walletLastTransaction(long walletAccountId, int limit);

	void insertDeposit(TransactionEntity transaction) throws InternalServiceException;

	void insertWithdraw(TransactionEntity transaction) throws InternalServiceException;
}
