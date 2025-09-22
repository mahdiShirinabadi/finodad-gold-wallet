package com.melli.wallet.service.operation;

import com.melli.wallet.domain.master.entity.ChannelEntity;
import com.melli.wallet.domain.master.entity.WalletAccountEntity;
import com.melli.wallet.domain.master.entity.WalletEntity;
import com.melli.wallet.exception.InternalServiceException;

import java.math.BigDecimal;

public interface WalletGiftCardLimitationOperationService {
	void checkGeneral(ChannelEntity channel, WalletEntity wallet, BigDecimal amount, WalletAccountEntity walletAccount) throws InternalServiceException;
	void checkDailyLimitation(ChannelEntity channel, WalletEntity wallet, BigDecimal amount, WalletAccountEntity walletAccount, String uniqueIdentifier) throws InternalServiceException;
	void updateLimitation(WalletAccountEntity walletAccount, BigDecimal quantity, String uniqueIdentifier) throws InternalServiceException;

}
