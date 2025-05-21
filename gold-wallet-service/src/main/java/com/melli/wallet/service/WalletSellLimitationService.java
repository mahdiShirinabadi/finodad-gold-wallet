package com.melli.wallet.service;

import com.melli.wallet.domain.master.entity.ChannelEntity;
import com.melli.wallet.domain.master.entity.WalletAccountEntity;
import com.melli.wallet.domain.master.entity.WalletEntity;
import com.melli.wallet.exception.InternalServiceException;

import java.math.BigDecimal;

public interface WalletSellLimitationService {
	void checkGeneral(ChannelEntity channel, WalletEntity wallet, BigDecimal amount, WalletAccountEntity walletAccount, String uniqueIdentifier) throws InternalServiceException;
	void checkDailyLimitation(ChannelEntity channel, WalletEntity wallet, BigDecimal amount, WalletAccountEntity walletAccount, String uniqueIdentifier) throws InternalServiceException;
	void checkMonthlyLimitation(ChannelEntity channel, WalletEntity wallet, BigDecimal amount, WalletAccountEntity walletAccount, String uniqueIdentifier) throws InternalServiceException;
	void updateLimitation(WalletAccountEntity walletAccount, BigDecimal amount, String uniqueIdentifier) throws InternalServiceException;
	void deleteAll();
}
