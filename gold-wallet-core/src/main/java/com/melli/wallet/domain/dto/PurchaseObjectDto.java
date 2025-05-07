package com.melli.wallet.domain.dto;

import com.melli.wallet.domain.master.entity.ChannelEntity;
import com.melli.wallet.domain.master.entity.MerchantEntity;
import com.melli.wallet.domain.master.entity.WalletAccountEntity;
import com.melli.wallet.domain.master.entity.WalletEntity;
import lombok.*;

import java.math.BigDecimal;

/**
 * Class Name: SellObjectDto
 * Author: Mahdi Shirinabadi
 * Date: 5/6/2025
 */
@Setter
@Getter
@Builder
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class PurchaseObjectDto {

    private ChannelEntity channel;
    private String uniqueIdentifier;
    private BigDecimal amount;
    private BigDecimal price;
    private BigDecimal commission;
    private String additionalData;
    private String nationalCode;
    private WalletEntity userWallet;
    private WalletAccountEntity userRialAccount;
    private WalletAccountEntity userCurrencyAccount;
    private MerchantEntity merchant;
    private WalletAccountEntity merchantRialAccount;
    private WalletAccountEntity merchantCurrencyAccount;
    private WalletAccountEntity channelCommissionAccount;
}
