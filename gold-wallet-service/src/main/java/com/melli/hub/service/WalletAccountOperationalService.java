package com.melli.hub.service;

import com.tara.wallet.exception.ServiceException;
import com.tara.wallet.master.domain.Channel;
import com.tara.wallet.response.*;

import java.util.List;

/**
 * @author ShirinAbadi.Mahdi on 6/18/2022
 * @project wallet-api
 */
public interface WalletAccountOperationalService {

    CreateWalletAccountResponse createAccountWallet(Channel channel, String mobile, String accountType, String groupId, String channelIp, String customerIp) throws ServiceException;
    Response createSpecialEscrowAccountWallet(Channel channel, String mobile, String accountTypeCode, String count, String channelIp, String customerIp) throws ServiceException;
    WalletAccountResponse getWalletAccount(Channel channel, String mobile, String channelIp) throws ServiceException;
    WalletAccountByWalletIdResponse getAllWalletAccount(Channel channel, String mobile, String channelIp, String walletId) throws ServiceException;
    WalletAccountResponse getWalletAccountInfoWithAccountNumber(Channel channel, String accountNumber, String channelIp) throws ServiceException;
    WalletAccountReportResponse getWalletAccountReport(Channel channel, String mobile, Integer page, Integer size) throws ServiceException;
    CreateWalletAccountResponse refreshSecretKey(Channel channel, String mobile, String account, String channelIp, String customerIp) throws ServiceException;
    Response deactivateAccountNumber(Channel channel, String mobile, String accountNumber, String channelIp) throws ServiceException;
    Response activateAccountNumber(Channel channel, String mobile, String accountNumber, String channelIp) throws ServiceException;
    WalletAccountNumberReportResponse getAccountNumberReport(String mobile, Integer page, Integer size) throws ServiceException;
    Response deleteWalletAccountNumber(String mobile, String accountNumber,String customerIp)throws ServiceException;

    Response updateWalletRefundCashbackBlackList(String mobile,String month,String year)throws ServiceException;
    Response settlementCashOutManually(int id,String mobile)throws ServiceException;
    Response ListSettlementCashOutManually(List<Integer> id)throws ServiceException;

}
