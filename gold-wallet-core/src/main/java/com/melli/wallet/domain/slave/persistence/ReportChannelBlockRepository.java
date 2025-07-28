package com.melli.wallet.domain.slave.persistence;

import com.melli.wallet.domain.slave.entity.ReportChannelBlockEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReportChannelBlockRepository extends CrudRepository<ReportChannelBlockEntity, Long> {
	ReportChannelBlockEntity findByChannelEntityId(long channelId);
} 