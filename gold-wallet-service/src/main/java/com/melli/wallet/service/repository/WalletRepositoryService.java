package com.melli.wallet.service.repository;

import com.melli.wallet.domain.master.entity.WalletEntity;
import com.melli.wallet.domain.slave.entity.ReportWalletEntity;
import com.melli.wallet.exception.InternalServiceException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface WalletRepositoryService {

    int ACTIVE = 1;
    int DISABLED = 2;

    public String WALLET_MERCHANT_PREFIX = "7";
    public String WALLET_CHANNEL_PREFIX = "8";
    public String WALLET_NORMAL_PREFIX = "9";

    short LEVEL_1 = 1;
    short LEVEL_2 = 2;

    WalletEntity findByNationalCodeAndWalletTypeId(String nationalCode, long walletTypeEntityId);

    WalletEntity findById(Long walletId) throws InternalServiceException;

    void save(WalletEntity wallet);

    List<WalletEntity> findAllByStatus(String status);

    List<WalletEntity> findWalletsWithFilters(String status, String nationalCode, String mobile);

    Page<ReportWalletEntity> findWalletsWithFiltersAndPagination(String status, String nationalCode, String mobile,
                                                                 String fromTime, String toTime, Pageable pageable);

    void clearAllCache();
}
