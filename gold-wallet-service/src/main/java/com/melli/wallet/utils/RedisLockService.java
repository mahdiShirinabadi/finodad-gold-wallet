package com.melli.wallet.utils;


import com.melli.wallet.exception.InternalServiceException;
import com.melli.wallet.service.repository.StatusRepositoryService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.integration.redis.util.RedisLockRegistry;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

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
        log.info("=== REDIS LOCK ACQUISITION START ===");
        log.info("Lock parameters - key: {}, class: {}, traceId: {}", lockKey, tClass.getSimpleName(), traceNumber);
        
        Lock lock = redisLockRegistry.obtain(lockKey);
        log.debug("Lock object obtained from registry for key: {}", lockKey);
        
        boolean lockSuccess = false;
        long lockStartTime = System.currentTimeMillis();
        
        try {
            log.info("{}  acquisition status Lock lockKey: {}, traceId ({}), class ({})", tClass.getSimpleName(), lockKey, traceNumber, tClass.getSimpleName());
            log.debug("Attempting to acquire lock with timeout: 40 seconds");
            
            lockSuccess = lock.tryLock(40L, TimeUnit.SECONDS);
            long lockAcquisitionTime = System.currentTimeMillis() - lockStartTime;
            
            if (lockSuccess) {
                log.info("lockSuccess acquisition status: true for lockKey:({}), traceId ({}), class ({}), acquisitionTime: {}ms", 
                    lockKey, traceNumber, tClass.getSimpleName(), lockAcquisitionTime);
                
                log.debug("Executing action within lock - class: {}", tClass.getSimpleName());
                long actionStartTime = System.currentTimeMillis();
                T result = action.get();
                long actionExecutionTime = System.currentTimeMillis() - actionStartTime;
                
                log.info("Action executed successfully - class: {}, executionTime: {}ms", tClass.getSimpleName(), actionExecutionTime);
                return result;
            } else {
                log.error("Lock acquisition failed - timeout reached for lockKey: {}, traceId: {}, class: {}, acquisitionTime: {}ms", 
                    lockKey, traceNumber, tClass.getSimpleName(), lockAcquisitionTime);
                throw new InternalServiceException("general error", StatusRepositoryService.ERROR_IN_LOCK, HttpStatus.OK);
            }
        } catch (InterruptedException e) {
            long totalTime = System.currentTimeMillis() - lockStartTime;
            log.error("Lock acquisition interrupted - lockKey: {}, traceId: {}, class: {}, error: {}, totalTime: {}ms", 
                lockKey, traceNumber, tClass.getSimpleName(), e.getMessage(), totalTime);
            Thread.currentThread().interrupt(); // Restore interrupted status
            throw new InternalServiceException("general error for try lock with lockKey " + lockKey, StatusRepositoryService.ERROR_IN_LOCK, HttpStatus.OK);
        } catch (Exception e) {
            long totalTime = System.currentTimeMillis() - lockStartTime;
            log.error("Unexpected error during lock operation - lockKey: {}, traceId: {}, class: {}, error: {}, totalTime: {}ms", 
                lockKey, traceNumber, tClass.getSimpleName(), e.getMessage(), totalTime);
            throw e;
        } finally {
            if (lockSuccess) {
                log.info("start unlock lockKey {} in class {}, traceNumber ({}) ", lockKey, tClass.getSimpleName(), traceNumber);
                long unlockStartTime = System.currentTimeMillis();
                lock.unlock();
                long unlockTime = System.currentTimeMillis() - unlockStartTime;
                log.info("finish unlock lockKey {} in class {}, traceNumber ({}), unlockTime: {}ms", 
                    lockKey, tClass.getSimpleName(), traceNumber, unlockTime);
            } else {
                log.warn("No unlock required - lock was not acquired for key: {}, traceId: {}", lockKey, traceNumber);
            }
            
            long totalOperationTime = System.currentTimeMillis() - lockStartTime;
            log.info("finish lock with key ({}), traceNumber ({}), class ({}), totalOperationTime: {}ms", 
                lockKey, traceNumber, tClass.getSimpleName(), totalOperationTime);
            log.info("=== REDIS LOCK OPERATION COMPLETED ===");
        }
    }

    /**
     * Acquire Redis lock and hold it until the surrounding Spring transaction commits.
     * If no transaction is active, the lock will be released immediately after action execution.
     */
    public <T, S> T runWithLockUntilCommit(String lockKey, Class<S> tClass, CustomSupplier<T> action, String traceNumber) throws InternalServiceException {
        log.info("start lock(with commit hold) with key ({})", lockKey);
        Lock lock = redisLockRegistry.obtain(lockKey);
        boolean lockSuccess = false;
        try {
            log.info("{} acquisition status Lock(with commit hold) lockKey: {}, traceId ({}), class ({})", tClass.getSimpleName(), lockKey, traceNumber, tClass.getSimpleName());
            lockSuccess = lock.tryLock(40L, TimeUnit.SECONDS);
            if (!lockSuccess) {
                log.error("system can not lock lockKey {} (with commit hold), traceId ({}), class ({})", lockKey, traceNumber, tClass.getSimpleName());
                throw new InternalServiceException("general error", StatusRepositoryService.ERROR_IN_LOCK, HttpStatus.OK);
            }

            final boolean txActive = TransactionSynchronizationManager.isSynchronizationActive();
            if (txActive) {
                final Lock heldLock = lock;
                TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {

                    @Override
                    public void afterCommit() {
                        // Release lock ONLY after successful commit
                        try {
                            log.info("afterCommit unlock lockKey {} in class {}, traceNumber ({})", lockKey, tClass.getSimpleName(), traceNumber);
                            heldLock.unlock();
                        } catch (Exception e) {
                            log.error("error in afterCommit unlock for lockKey {}: {}", lockKey, e.getMessage());
                        }
                    }

                    @Override
                    public void afterCompletion(int status) {
                        // Handle rollback case
                        if (status == STATUS_ROLLED_BACK) {
                            try {
                                log.info("afterRollback unlock lockKey {} in class {}, traceNumber ({})", lockKey, tClass.getSimpleName(), traceNumber);
                                heldLock.unlock();
                            } catch (Exception e) {
                                log.error("error in afterRollback unlock for lockKey {}: {}", lockKey, e.getMessage());
                            }
                        }
                    }
                });
            }

            // Execute user action under lock
            return action.get();
        } catch (InterruptedException e) {
            log.error("interrupt exception for try lock lockKey {}(with commit hold) and error is {} , traceId ({}), class ({})", lockKey, e.getMessage(), traceNumber, tClass.getSimpleName());
            throw new InternalServiceException("general error for try lock with lockKey " + lockKey, StatusRepositoryService.ERROR_IN_LOCK, HttpStatus.OK);
        } finally {
            boolean txActive = TransactionSynchronizationManager.isSynchronizationActive();
            if (lockSuccess && !txActive) {
                // No transaction: unlock immediately
                try {
                    log.info("immediate unlock (no-tx) lockKey {} in class {}, traceNumber ({})", lockKey, tClass.getSimpleName(), traceNumber);
                    lock.unlock();
                } catch (Exception e) {
                    log.error("error in immediate unlock for lockKey {}: {}", lockKey, e.getMessage());
                }
            }
            log.info("finish lock(with commit hold) with key ({}), traceNumber ({}), class ({})", lockKey, traceNumber, tClass.getSimpleName());
        }
    }

}
