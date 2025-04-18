package com.melli.wallet.service;

import com.melli.wallet.domain.master.entity.ChannelEntity;
import com.melli.wallet.domain.master.entity.MerchantEntity;
import com.melli.wallet.domain.master.entity.WalletAccountEntity;
import com.melli.wallet.domain.master.entity.WalletEntity;
import com.melli.wallet.domain.redis.WalletLimitationRedis;
import com.melli.wallet.domain.redis.WalletMonthlyLimitationRedis;
import com.melli.wallet.exception.InternalServiceException;

public interface WalletLimitationService {

	void save(WalletLimitationRedis walletLimitation);

	void saveMonthly(WalletMonthlyLimitationRedis walletMonthlyLimitation);

	void checkPurchaseLimitation(ChannelEntity channel, WalletEntity wallet, long amount, WalletAccountEntity walletAccount, MerchantEntity merchant) throws InternalServiceException;

	void checkCashInLimitation(ChannelEntity channel, WalletEntity wallet, long amount, WalletAccountEntity walletAccount) throws InternalServiceException;

	void checkCashOutLimitation(ChannelEntity channel, WalletEntity wallet, long amount, WalletAccountEntity walletAccount) throws InternalServiceException;
		
	void checkP2PLimitation(ChannelEntity channel, WalletEntity wallet, WalletAccountEntity sourceWalletAccount, WalletAccountEntity destinationWalletAccount, long amount) throws InternalServiceException;
	
	void checkBlockLimitation(ChannelEntity channel, WalletAccountEntity walletAccount, long amount) throws InternalServiceException;

	void updatePurchaseLimitation(WalletAccountEntity wallet, long amount, MerchantEntity merchantEntity);

	void updateCashInLimitation(WalletAccountEntity wallet, long amount) throws InternalServiceException;

	void updateCashOutLimitation(WalletAccountEntity wallet, long amount) throws InternalServiceException;
	
	void updateP2PLimitation(WalletAccountEntity walletAccount, long amount) throws InternalServiceException;
	
	void updateBlockLimitation(WalletAccountEntity walletAccount, long amount) throws InternalServiceException;
	
	void updateUnBlockLimitation(WalletAccountEntity walletAccount, long amount) throws InternalServiceException;
}
