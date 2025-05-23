package com.melli.wallet.service;

import com.melli.wallet.domain.master.entity.ChannelEntity;
import com.melli.wallet.domain.master.entity.WalletAccountEntity;
import com.melli.wallet.domain.master.entity.WalletEntity;
import com.melli.wallet.domain.redis.WalletCashInLimitationRedis;
import com.melli.wallet.exception.InternalServiceException;

import java.math.BigDecimal;

public interface WalletCashLimitationService {

	void checkCashInLimitation(ChannelEntity channel, WalletEntity wallet, BigDecimal amount, WalletAccountEntity walletAccount) throws InternalServiceException;

	void updateCashInLimitation(WalletAccountEntity wallet, BigDecimal amount) throws InternalServiceException;

}
