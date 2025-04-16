package com.melli.wallet.service;


import com.melli.wallet.domain.master.entity.ShahkarInfoEntity;

import java.util.Optional;

public interface ShahkarInfoService {
    void save(ShahkarInfoEntity shahkarInfoEntity);
    Optional<ShahkarInfoEntity> findTopByMobileAndNationalCodeAndISMatchOrderById(String mobile, String nationalCode, Boolean isMatch);
}
