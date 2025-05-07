package com.melli.wallet.service;

import com.melli.wallet.domain.master.entity.ChannelEntity;
import com.melli.wallet.domain.response.UuidResponse;
import com.melli.wallet.domain.response.cash.CashInResponse;
import com.melli.wallet.domain.response.cash.CashInTrackResponse;
import com.melli.wallet.domain.response.cash.CashOutTrackResponse;
import com.melli.wallet.exception.InternalServiceException;


public interface CashOutService {

	UuidResponse generateUuid(ChannelEntity channelEntity, String nationalCode, String amount, String accountNumber) throws InternalServiceException;
	CashInResponse withdrawal(ChannelEntity channelEntity, String nationalCode, String uniqueIdentifier, String amount, String refNumber, String signData, String dataForCheckInVerify, String accountNumber, String additionalData, String ip) throws InternalServiceException;
	CashOutTrackResponse inquiry(ChannelEntity channelEntity, String uuid, String channelIp)throws InternalServiceException;
}
