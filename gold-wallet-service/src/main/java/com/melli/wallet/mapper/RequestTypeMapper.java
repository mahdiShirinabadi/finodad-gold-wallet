package com.melli.wallet.mapper;

import com.melli.wallet.domain.master.entity.RequestTypeEntity;
import com.melli.wallet.domain.slave.entity.ReportRequestTypeEntity;
import java.util.Collections;
import java.util.Collections;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class RequestTypeMapper {

    public RequestTypeEntity toRequestTypeEntity(ReportRequestTypeEntity reportEntity) {
        if (reportEntity == null) {
            return null;
        }
        
        RequestTypeEntity entity = RequestTypeEntity.builder()
                .name(reportEntity.getName())
                .build();
        // Always set ID
        entity.setId(reportEntity.getId());
        return entity;
    }

    public ReportRequestTypeEntity toReportRequestTypeEntity(RequestTypeEntity entity) {
        if (entity == null) {
            return null;
        }
        
        ReportRequestTypeEntity reportEntity = ReportRequestTypeEntity.builder()
                .name(entity.getName())
                .build();
        // Always set ID
        reportEntity.setId(entity.getId());
        return reportEntity;
    }

    public List<RequestTypeEntity> toRequestTypeEntityList(List<ReportRequestTypeEntity> reportEntities) {
        if (reportEntities == null) {
            return Collections.emptyList();
        }
        
        return reportEntities.stream()
                .map(this::toRequestTypeEntity)
                .toList();
    }

    public List<ReportRequestTypeEntity> toReportRequestTypeEntityList(List<RequestTypeEntity> entities) {
        if (entities == null) {
            return Collections.emptyList();
        }
        
        return entities.stream()
                .map(this::toReportRequestTypeEntity)
                .toList();
    }
}
