package com.melli.wallet.domain.dto;

import java.math.BigDecimal;

public interface BalanceDTO {
    BigDecimal getRealBalance();
    BigDecimal getAvailableBalance();
}
