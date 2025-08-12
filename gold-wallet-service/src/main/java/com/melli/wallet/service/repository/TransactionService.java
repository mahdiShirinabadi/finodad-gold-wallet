package com.melli.wallet.service.repository;

import com.melli.wallet.domain.master.entity.TransactionEntity;
import com.melli.wallet.domain.slave.entity.ReportTransactionEntity;
import com.melli.wallet.exception.InternalServiceException;
import org.springframework.data.domain.Page;

import java.util.List;

public interface TransactionService {

	List<TransactionEntity> walletLastTransaction(long walletAccountId, int limit);

	Page<ReportTransactionEntity> reportWalletLastTransaction(long walletAccountId, int limit, int page);

	void insertDeposit(TransactionEntity transaction) throws InternalServiceException;

	void insertWithdraw(TransactionEntity transaction) throws InternalServiceException;
}
