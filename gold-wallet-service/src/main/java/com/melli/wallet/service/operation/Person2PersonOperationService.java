package com.melli.wallet.service.operation;

import com.melli.wallet.domain.dto.ChargeObjectDTO;
import com.melli.wallet.domain.dto.P2pObjectDTO;
import com.melli.wallet.domain.master.entity.ChannelEntity;
import com.melli.wallet.domain.response.UuidResponse;
import com.melli.wallet.domain.response.cash.CashInResponse;
import com.melli.wallet.domain.response.cash.CashInTrackResponse;
import com.melli.wallet.domain.response.p2p.P2pTrackResponse;
import com.melli.wallet.domain.response.p2p.P2pUuidResponse;
import com.melli.wallet.exception.InternalServiceException;


public interface Person2PersonOperationService {
	P2pUuidResponse generateUuid(ChannelEntity channelEntity, String nationalCode, String amount, String accountNumber, String destAccountNumber) throws InternalServiceException;
	void process(P2pObjectDTO p2pObjectDTO) throws InternalServiceException;
	P2pTrackResponse inquiry(ChannelEntity channel, String uuid, String channelIp)throws InternalServiceException;
}
