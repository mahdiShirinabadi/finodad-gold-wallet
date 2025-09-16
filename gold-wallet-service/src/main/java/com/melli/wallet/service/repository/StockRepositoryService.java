package com.melli.wallet.service.repository;

import com.melli.wallet.domain.master.entity.TransactionEntity;
import com.melli.wallet.domain.master.entity.WalletAccountCurrencyEntity;
import com.melli.wallet.domain.response.stock.StockCurrencyListResponse;
import com.melli.wallet.domain.response.stock.StockListResponse;
import com.melli.wallet.exception.InternalServiceException;

public interface StockRepositoryService {

	StockListResponse getAllBalance(WalletAccountCurrencyEntity walletAccountCurrencyEntity);
	StockCurrencyListResponse getSumBalanceByCurrency();
	void insertDeposit(TransactionEntity transaction) throws InternalServiceException;
	void insertWithdraw(TransactionEntity transaction) throws InternalServiceException;
}
