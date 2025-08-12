package com.melli.wallet.mapper;

import com.melli.wallet.domain.master.entity.WalletTypeEntity;
import com.melli.wallet.domain.slave.entity.ReportWalletTypeEntity;
import java.util.Collections;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class WalletTypeMapper {

    public WalletTypeEntity toWalletTypeEntity(ReportWalletTypeEntity reportEntity) {
        if (reportEntity == null) {
            return null;
        }
        
        WalletTypeEntity entity = WalletTypeEntity.builder()
                .name(reportEntity.getName())
                .build();
        // Always set ID
        entity.setId(reportEntity.getId());
        return entity;
    }

    public ReportWalletTypeEntity toReportWalletTypeEntity(WalletTypeEntity entity) {
        if (entity == null) {
            return null;
        }
        
        ReportWalletTypeEntity reportEntity = ReportWalletTypeEntity.builder()
                .name(entity.getName())
                .build();
        // Always set ID
        reportEntity.setId(entity.getId());
        return reportEntity;
    }

    public List<WalletTypeEntity> toWalletTypeEntityList(List<ReportWalletTypeEntity> reportEntities) {
        if (reportEntities == null) {
            return Collections.emptyList();
        }
        
        return reportEntities.stream()
                .map(this::toWalletTypeEntity)
                .toList();
    }

    public List<ReportWalletTypeEntity> toReportWalletTypeEntityList(List<WalletTypeEntity> entities) {
        if (entities == null) {
            return Collections.emptyList();
        }
        
        return entities.stream()
                .map(this::toReportWalletTypeEntity)
                .toList();
    }
}
