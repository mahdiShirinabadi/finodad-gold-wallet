package com.melli.hub.service;

import com.melli.hub.domain.master.entity.ChannelEntity;
import com.melli.hub.domain.response.cash.CashInResponse;
import com.melli.hub.domain.response.cash.CashInTrackResponse;
import com.melli.hub.exception.InternalServiceException;


public interface CashService {


	/***
	 * be 2soorat mitavan charge kar:
	 * 1- purchase web  baraye channel true
	 * 2- IPG finnopay, baraye channel normal
	 * va channel baraye traceId marboot be on tarakonesh ro dar refNumber ersal konad
	 * een method bayad be soorat sync bashad.
	 * @param uid  traceId user get before in call getRrn
	 * @param amount amount for charge
	 * @param refNumber refNumber for wallet charge in finnopay system
	 * @param channelIp ip channel, this ip get from header
	 * @param customerIp user ip, ip for endUser
	 * @param signData signData
	 * @param dataForCheckInVerify necessary data for check with sign data
	 * @param accountNumber count number
	 * @return CashInResponse if success ,if error occurred in this method throw service exception,
	 * @throws InternalServiceException if error occurred
	 */
	CashInResponse cashIn(ChannelEntity channel, String nationalCode, String uniqueIdentifier, String amount, String refNumber, String signData, String dataForCheckInVerify, String accountNumber, String additionalData, String ip) throws InternalServiceException;
//	CashOutResponse cashOut(ChannelEntity channel, String pin, String uid, String accountNumber, String amount, String destIban, String channelIp, String ip, String signData, String dataForCheckInVerify, String nationalCode, String birthDate) throws InternalServiceException;
//	DechargeResponse decharge(ChannelEntity channel, String uid, String accountNumber, String amount, String channelIp, String description, String additionalData,String walletId, String signData, String dataForCheckInVerify) throws InternalServiceException;
//	CashOutTrackResponse trackCashOut(ChannelEntity channel, String uid) throws InternalServiceException;
	CashInTrackResponse cashInTrack(String uid, String channelIp)throws InternalServiceException;
//	DechargeTrackResponse deChargeTrack(String uid, String channelIp)throws InternalServiceException;
}
