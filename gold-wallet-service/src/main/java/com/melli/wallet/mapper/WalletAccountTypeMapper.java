package com.melli.wallet.mapper;

import com.melli.wallet.domain.master.entity.WalletAccountTypeEntity;
import com.melli.wallet.domain.slave.entity.ReportWalletAccountTypeEntity;
import java.util.Collections;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class WalletAccountTypeMapper {

    public WalletAccountTypeEntity toWalletAccountTypeEntity(ReportWalletAccountTypeEntity reportEntity) {
        if (reportEntity == null) {
            return null;
        }
        
        WalletAccountTypeEntity entity = WalletAccountTypeEntity.builder()
                .name(reportEntity.getName())
                .build();
        // Always set ID
        entity.setId(reportEntity.getId());
        return entity;
    }

    public ReportWalletAccountTypeEntity toReportWalletAccountTypeEntity(WalletAccountTypeEntity entity) {
        if (entity == null) {
            return null;
        }
        
        ReportWalletAccountTypeEntity reportEntity = ReportWalletAccountTypeEntity.builder()
                .name(entity.getName())
                .build();
        // Always set ID
        reportEntity.setId(entity.getId());
        return reportEntity;
    }

    public List<WalletAccountTypeEntity> toWalletAccountTypeEntityList(List<ReportWalletAccountTypeEntity> reportEntities) {
        if (reportEntities == null) {
            return Collections.emptyList();
        }
        
        return reportEntities.stream()
                .map(this::toWalletAccountTypeEntity)
                .toList();
    }

    public List<ReportWalletAccountTypeEntity> toReportWalletAccountTypeEntityList(List<WalletAccountTypeEntity> entities) {
        if (entities == null) {
            return Collections.emptyList();
        }
        
        return entities.stream()
                .map(this::toReportWalletAccountTypeEntity)
                .toList();
    }
}
