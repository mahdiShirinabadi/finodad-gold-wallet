package com.melli.wallet.service.repository.impl;

import com.melli.wallet.domain.master.entity.ShedLockEntity;
import com.melli.wallet.domain.master.persistence.ShedLockRepository;
import com.melli.wallet.domain.response.panel.PanelShedlockResponse;
import com.melli.wallet.exception.InternalServiceException;
import com.melli.wallet.service.repository.SettingGeneralRepositoryService;
import com.melli.wallet.service.repository.ShedlockRepositoryService;
import com.melli.wallet.service.repository.StatusRepositoryService;
import com.melli.wallet.util.date.DateUtils;
import com.melli.wallet.utils.Helper;
import com.melli.wallet.utils.RedisLockService;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.flywaydb.core.internal.util.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.*;

import static com.melli.wallet.utils.SubHelper.FORMAT_DATE_RESPONSE;
import static java.util.Map.entry;


@Log4j2
@Service
@RequiredArgsConstructor
public class ShedlockRepositoryServiceImplementation implements ShedlockRepositoryService {
    private final ShedLockRepository repository;
    private final RedisLockService redisLockService;
    private final Helper helper;
    private final SettingGeneralRepositoryService settingGeneralRepositoryService;

    @Override
    public PanelShedlockResponse findAll(Map<String, String> mapParameter) {
        if (mapParameter == null) {
            mapParameter = new HashMap<>();
        }
        Pageable pageRequest = helper.getPageableConfig(settingGeneralRepositoryService, Integer.parseInt(mapParameter.get("page")), Integer.parseInt(mapParameter.get("size")));
        Page<ShedLockEntity> entityList = repository.findAll(getPredicate(mapParameter), pageRequest);
        return helper.fillPanelShedlockResponse(entityList);
    }

    @Override
    public ShedLockEntity findByName(String name) {
        return repository.findByName(name);
    }

    @Override
    public Integer deleteByName(String name) {
        return repository.deleteByName(name);
    }

    public void checkActiveLock(String name) throws InternalServiceException {
        redisLockService.runAfterLock(name, this.getClass(), ()->{
            ShedLockEntity shedLockEntity = findByName(name);
            if(shedLockEntity != null){
                Date now = new Date();
                if(now.before(shedLockEntity.getLockUntil())){
                    log.error("shedlock for job ({}) is running until ({})",name, shedLockEntity.getLockUntil());
                    throw new InternalServiceException("job is running", StatusRepositoryService.JOB_IS_RUNNING, HttpStatus.OK, Map.ofEntries(
                            entry("1", shedLockEntity.getName()),
                            entry("2", DateUtils.getLocaleDate(DateUtils.FARSI_LOCALE, shedLockEntity.getLockUntil(), FORMAT_DATE_RESPONSE, false))
                    ));
                }
            }
            return null;
        },"");
    }

    private Specification<ShedLockEntity> getPredicate(Map<String, String> searchCriteria) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = buildPredicatesFromCriteria(searchCriteria, root, criteriaBuilder);
            String orderBy = Optional.ofNullable(searchCriteria.get("orderBy")).orElse("lockAt");
            String sortDirection = Optional.ofNullable(searchCriteria.get("sort")).orElse("desc");

            setOrder(query, orderBy, sortDirection, criteriaBuilder, root);

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    private List<Predicate> buildPredicatesFromCriteria(Map<String, String> searchCriteria, Root<ShedLockEntity> root, CriteriaBuilder criteriaBuilder) {
        List<Predicate> predicates = new ArrayList<>();

        if (StringUtils.hasText(searchCriteria.get("id"))) {
            predicates.add(criteriaBuilder.equal(root.get("id"), searchCriteria.get("id")));
        }
        return predicates;
    }

    private void setOrder(CriteriaQuery<?> query, String orderBy, String sortDirection, CriteriaBuilder criteriaBuilder, Root<ShedLockEntity> root) {
        if ("asc".equalsIgnoreCase(sortDirection)) {
            query.orderBy(criteriaBuilder.asc(root.get(orderBy)));
        } else {
            query.orderBy(criteriaBuilder.desc(root.get(orderBy)));
        }
    }
}
