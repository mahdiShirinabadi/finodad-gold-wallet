package com.melli.wallet.mapper;

import com.melli.wallet.domain.master.entity.StatusEntity;
import com.melli.wallet.domain.slave.entity.ReportStatusEntity;
import java.util.Collections;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class StatusMapper {

    public StatusEntity toStatusEntity(ReportStatusEntity reportEntity) {
        if (reportEntity == null) {
            return null;
        }
        
        StatusEntity entity = StatusEntity.builder()
                .code(reportEntity.getCode())
                .persianDescription(reportEntity.getPersianDescription())
                .additionalData(reportEntity.getAdditionalData())
                .build();
        // Always set ID
        entity.setId(reportEntity.getId());
        return entity;
    }

    public ReportStatusEntity toReportStatusEntity(StatusEntity entity) {
        if (entity == null) {
            return null;
        }
        
        ReportStatusEntity reportEntity = ReportStatusEntity.builder()
                .code(entity.getCode())
                .persianDescription(entity.getPersianDescription())
                .additionalData(entity.getAdditionalData())
                .build();
        // Always set ID
        reportEntity.setId(entity.getId());
        return reportEntity;
    }

    public List<StatusEntity> toStatusEntityList(List<ReportStatusEntity> reportEntities) {
        if (reportEntities == null) {
            return Collections.emptyList();
        }
        
        return reportEntities.stream()
                .map(this::toStatusEntity)
                .toList();
    }

    public List<ReportStatusEntity> toReportStatusEntityList(List<StatusEntity> entities) {
        if (entities == null) {
            return Collections.emptyList();
        }
        
        return entities.stream()
                .map(this::toReportStatusEntity)
                .toList();
    }
}
