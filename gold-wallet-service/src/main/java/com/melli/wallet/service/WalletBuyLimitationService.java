package com.melli.wallet.service;

import com.melli.wallet.domain.master.entity.ChannelEntity;
import com.melli.wallet.domain.master.entity.MerchantEntity;
import com.melli.wallet.domain.master.entity.WalletAccountEntity;
import com.melli.wallet.domain.master.entity.WalletEntity;
import com.melli.wallet.exception.InternalServiceException;

import java.math.BigDecimal;

public interface WalletBuyLimitationService {
	void checkBuyGeneral(ChannelEntity channel, WalletEntity wallet, BigDecimal amount, WalletAccountEntity walletAccount, String uniqueIdentifier) throws InternalServiceException;
	void checkBuyDailyLimitation(ChannelEntity channel, WalletEntity wallet, BigDecimal amount, WalletAccountEntity walletAccount, MerchantEntity merchant, String uniqueIdentifier) throws InternalServiceException;
	void checkBuyMonthlyLimitation(ChannelEntity channel, WalletEntity wallet, BigDecimal amount, WalletAccountEntity walletAccount, MerchantEntity merchant, String uniqueIdentifier) throws InternalServiceException;
	void updateBuyDailyLimitation(WalletAccountEntity walletAccount, BigDecimal amount) throws InternalServiceException;
	void updateBuyMonthlyLimitation(WalletAccountEntity walletAccount, BigDecimal amount) throws InternalServiceException;
}
