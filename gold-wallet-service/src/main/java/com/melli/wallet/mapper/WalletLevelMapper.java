package com.melli.wallet.mapper;

import com.melli.wallet.domain.master.entity.WalletLevelEntity;
import com.melli.wallet.domain.slave.entity.ReportWalletLevelEntity;
import java.util.Collections;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class WalletLevelMapper {

    public WalletLevelEntity toWalletLevelEntity(ReportWalletLevelEntity reportEntity) {
        if (reportEntity == null) {
            return null;
        }
        
        WalletLevelEntity entity = WalletLevelEntity.builder()
                .name(reportEntity.getName())
                .build();
        // Always set ID
        entity.setId(reportEntity.getId());
        return entity;
    }

    public ReportWalletLevelEntity toReportWalletLevelEntity(WalletLevelEntity entity) {
        if (entity == null) {
            return null;
        }
        
        ReportWalletLevelEntity reportEntity = ReportWalletLevelEntity.builder()
                .name(entity.getName())
                .build();
        // Always set ID
        reportEntity.setId(entity.getId());
        return reportEntity;
    }

    public List<WalletLevelEntity> toWalletLevelEntityList(List<ReportWalletLevelEntity> reportEntities) {
        if (reportEntities == null) {
            return Collections.emptyList();
        }
        
        return reportEntities.stream()
                .map(this::toWalletLevelEntity)
                .toList();
    }

    public List<ReportWalletLevelEntity> toReportWalletLevelEntityList(List<WalletLevelEntity> entities) {
        if (entities == null) {
            return Collections.emptyList();
        }
        
        return entities.stream()
                .map(this::toReportWalletLevelEntity)
                .toList();
    }
}
