package com.melli.wallet.service;

import com.melli.wallet.domain.master.entity.ChannelEntity;
import com.melli.wallet.domain.response.cash.CashInResponse;
import com.melli.wallet.domain.response.cash.CashInTrackResponse;
import com.melli.wallet.exception.InternalServiceException;


public interface CashService {
	CashInResponse cashIn(ChannelEntity channel, String nationalCode, String uniqueIdentifier, String amount, String refNumber, String signData, String dataForCheckInVerify, String accountNumber, String additionalData, String ip) throws InternalServiceException;
	CashInTrackResponse cashInTrack(ChannelEntity channel, String uuid, String channelIp)throws InternalServiceException;
}
