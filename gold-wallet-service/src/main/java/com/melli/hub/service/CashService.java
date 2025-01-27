package com.melli.hub.service;

import com.melli.hub.domain.master.entity.ChannelEntity;
import com.melli.hub.domain.response.cash.CashInResponse;
import com.melli.hub.domain.response.cash.CashInTrackResponse;
import com.melli.hub.exception.InternalServiceException;


public interface CashService {
	CashInResponse cashIn(ChannelEntity channel, String nationalCode, String uniqueIdentifier, String amount, String refNumber, String signData, String dataForCheckInVerify, String accountNumber, String additionalData, String ip) throws InternalServiceException;
	CashInTrackResponse cashInTrack(ChannelEntity channel, String uuid, String channelIp)throws InternalServiceException;
}
