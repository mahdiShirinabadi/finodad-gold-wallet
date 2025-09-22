package com.melli.wallet.service.operation;

import com.melli.wallet.domain.dto.GiftCardPaymentObjectDTO;
import com.melli.wallet.domain.dto.GiftCardProcessObjectDTO;
import com.melli.wallet.domain.dto.P2pObjectDTO;
import com.melli.wallet.domain.master.entity.ChannelEntity;
import com.melli.wallet.domain.response.giftcard.GiftCardTrackResponse;
import com.melli.wallet.domain.response.giftcard.GiftCardUuidResponse;
import com.melli.wallet.domain.response.p2p.P2pTrackResponse;
import com.melli.wallet.domain.response.p2p.P2pUuidResponse;
import com.melli.wallet.exception.InternalServiceException;


public interface GiftCardOperationService {
	GiftCardUuidResponse generateUuid(ChannelEntity channelEntity, String nationalCode, String quantity, String accountNumber, String currency) throws InternalServiceException;
	void process(GiftCardProcessObjectDTO giftCardProcessObjectDTO) throws InternalServiceException;
	P2pUuidResponse payment(GiftCardPaymentObjectDTO giftCardPaymentObjectDTO) throws InternalServiceException;
	GiftCardTrackResponse inquiry(ChannelEntity channel, String uuid, String channelIp)throws InternalServiceException;
}
