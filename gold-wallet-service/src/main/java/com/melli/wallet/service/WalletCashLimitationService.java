package com.melli.wallet.service;

import com.melli.wallet.domain.master.entity.ChannelEntity;
import com.melli.wallet.domain.master.entity.WalletAccountEntity;
import com.melli.wallet.domain.master.entity.WalletEntity;
import com.melli.wallet.exception.InternalServiceException;

import java.math.BigDecimal;

public interface WalletCashLimitationService {

	void checkCashInLimitation(ChannelEntity channel, WalletEntity wallet, BigDecimal amount, WalletAccountEntity walletAccount) throws InternalServiceException;
	void checkCashOutLimitation(ChannelEntity channel, WalletEntity wallet, BigDecimal amount, WalletAccountEntity walletAccount) throws InternalServiceException;
	void checkPhysicalCashOutLimitation(ChannelEntity channel, WalletEntity wallet, BigDecimal quantity, WalletAccountEntity walletAccount) throws InternalServiceException;

	void updateCashInLimitation(WalletAccountEntity wallet, BigDecimal amount) throws InternalServiceException;
	void updateCashOutLimitation(WalletAccountEntity wallet, BigDecimal amount) throws InternalServiceException;
	void updatePhysicalCashOutLimitation(WalletAccountEntity wallet, BigDecimal quantity) throws InternalServiceException;

	void deleteAll();

}
