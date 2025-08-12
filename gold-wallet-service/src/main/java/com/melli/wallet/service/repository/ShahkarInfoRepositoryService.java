package com.melli.wallet.service.repository;


import com.melli.wallet.domain.master.entity.ShahkarInfoEntity;

import java.util.Optional;

public interface ShahkarInfoRepositoryService {
    void save(ShahkarInfoEntity shahkarInfoEntity);
    Optional<ShahkarInfoEntity> findTopByMobileAndNationalCodeAndISMatchOrderById(String mobile, String nationalCode, Boolean isMatch);
}
