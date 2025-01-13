package com.melli.hub.utils;


import com.melli.hub.exception.InternalServiceException;
import com.melli.hub.service.StatusService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.integration.redis.util.RedisLockRegistry;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

@Service
public class RedisLockService {

    private final RedisLockRegistry redisLockRegistry;

    private final Logger log = LogManager.getLogger(RedisLockService.class);

    public RedisLockService(RedisLockRegistry redisLockRegistry) {
        this.redisLockRegistry = redisLockRegistry;
    }

    public interface CustomSupplier<T> {
        T get() throws InternalServiceException;
    }


    public <T, S> T runAfterLock(String lockKey, Class<S> tClass, CustomSupplier<T> action, String traceNumber) throws InternalServiceException {
        log.info("start lock with key ({})", lockKey);
        Lock lock = redisLockRegistry.obtain(lockKey);
        boolean lockSuccess = false;
        try {
            log.info("{}  acquisition status Lock lockKey: {}, traceId ({}), class ({})", tClass.getSimpleName(), lockKey, traceNumber, tClass.getSimpleName());
            lockSuccess = lock.tryLock(40L, TimeUnit.SECONDS);
            if (lockSuccess) {
                log.info("lockSuccess acquisition status: true for lockKey:({}), traceId ({}), class ({})", lockKey, traceNumber, tClass.getSimpleName());
                return action.get();
            } else {
                log.error("system can not luck lockKey {}, traceId ({}), class ({})", lockKey, traceNumber, tClass.getSimpleName());
                throw new InternalServiceException("general error", StatusService.ERROR_IN_LOCK, HttpStatus.OK);
            }
        } catch (InterruptedException e) {
            log.error("interrupt exception for try lock lockKey {} and error is {} , traceId ({}), class ({})", lockKey, e.getMessage(), traceNumber, tClass.getSimpleName());
            throw new InternalServiceException("general error for try lock with lockKey " + lockKey, StatusService.ERROR_IN_LOCK, HttpStatus.OK);
        } finally {
            if (lockSuccess) {
                log.info("start unlock lockKey {} in class {}, traceNumber ({}) ", lockKey, tClass.getSimpleName(), traceNumber);
                lock.unlock();
                log.info("finish unlock lockKey {} in class {}, traceNumber ({})", lockKey, tClass.getSimpleName(), traceNumber);
            }
            log.info("finish lock with key ({}), traceNumber ({}), class ({})", lockKey, traceNumber, tClass.getSimpleName());
        }
    }

}
