package com.melli.hub.util;

import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class LockByKey {

    private static Set<String> usedKeys = ConcurrentHashMap.newKeySet();

    public boolean tryLock(String key) {
        return usedKeys.add(key);
    }

    public void unlock(String key) {
        usedKeys.remove(key);
    }

}
