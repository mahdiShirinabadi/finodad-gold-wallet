package com.melli.wallet.service.repository.impl;

import com.melli.wallet.domain.master.entity.ShahkarInfoEntity;
import com.melli.wallet.domain.master.persistence.ShahkarInfoRepository;
import com.melli.wallet.service.repository.ShahkarInfoRepositoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Log4j2
@RequiredArgsConstructor
public class ShahkarInfoRepositoryServiceImplementation implements ShahkarInfoRepositoryService {

    private final ShahkarInfoRepository shahkarInfoRepository;

    @Override
    public void save(ShahkarInfoEntity shahkarInfoEntity) {
        log.info("start save shahkarInfo entity ");
        shahkarInfoRepository.save(shahkarInfoEntity);
        log.info("success save shahkarInfo entity ");
    }

    @Override
    public Optional<ShahkarInfoEntity> findTopByMobileAndNationalCodeAndISMatchOrderById(String mobile, String nationalCode, Boolean isMatch) {
        return shahkarInfoRepository.findTopByMobileAndNationalCodeAndIsMatchOrderByIdDesc(mobile, nationalCode, isMatch);
    }
}
