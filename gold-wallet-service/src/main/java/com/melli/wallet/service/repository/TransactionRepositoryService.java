package com.melli.wallet.service.repository;

import com.melli.wallet.domain.master.entity.ChannelEntity;
import com.melli.wallet.domain.master.entity.TransactionEntity;
import com.melli.wallet.domain.response.transaction.ReportTransactionResponse;
import com.melli.wallet.domain.response.transaction.StatementResponse;
import com.melli.wallet.exception.InternalServiceException;

import java.util.Map;

public interface TransactionRepositoryService {

	StatementResponse lastTransaction(ChannelEntity channelEntity, String nationalCode, String walletAccountNumber, int limit) throws InternalServiceException;

	ReportTransactionResponse reportTransaction(ChannelEntity channelEntity, Map<String, String>mapParameter);

	void insertDeposit(TransactionEntity transaction) throws InternalServiceException;

	void insertWithdraw(TransactionEntity transaction) throws InternalServiceException;
}
