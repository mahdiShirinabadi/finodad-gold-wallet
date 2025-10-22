package com.melli.wallet.service.repository;

import com.melli.wallet.domain.master.entity.StockEntity;
import com.melli.wallet.domain.master.entity.TransactionEntity;
import com.melli.wallet.domain.master.entity.WalletAccountCurrencyEntity;
import com.melli.wallet.domain.response.stock.StockCurrencyListResponse;
import com.melli.wallet.domain.response.stock.StockListResponse;
import com.melli.wallet.exception.InternalServiceException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.Map;

public interface StockRepositoryService {

    BigDecimal getBalance(long id);
	StockListResponse getAllBalance(WalletAccountCurrencyEntity walletAccountCurrencyEntity);
	StockCurrencyListResponse getSumBalanceByCurrency();
	void insertDeposit(TransactionEntity transaction) throws InternalServiceException;
	void insertWithdraw(TransactionEntity transaction) throws InternalServiceException;
	
	Page<StockEntity> findAllWithSpecification(Map<String, String> searchCriteria, Pageable pageable);
}
