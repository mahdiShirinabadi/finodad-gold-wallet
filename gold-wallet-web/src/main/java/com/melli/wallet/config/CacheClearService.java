package com.melli.wallet.config;

import com.melli.wallet.utils.Constant;
import jakarta.annotation.PostConstruct;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Service
@Log4j2
public class CacheClearService {
    @Autowired
    private CacheManager cacheManager;

    @PostConstruct
    public void clearCache() {
        // Split the cache names from properties and clear each specified cache


        // Split the cache names from properties and clear each specified cache
        // Create a list to store the values of the constants
        List<String> constantValues = new ArrayList<>();

        // Get the Class object for the Constant class
        Class<Constant> constantClass = Constant.class;
        // Get all declared fields in the Constant class
        Field[] fields = constantClass.getDeclaredFields();

        // Iterate over the fields
        for (Field field : fields) {
            // Check if the field is a public static final String
            if (field.getType() == String.class && java.lang.reflect.Modifier.isStatic(field.getModifiers())) {
                try {
                    // Get the value of the field
                    String value = (String) field.get(null); // null because the field is static
                    // Add the value to the list
                    constantValues.add(value);
                } catch (IllegalAccessException ignored) {
                }
            }
        }

        constantValues.forEach(cacheName -> {
            if (cacheManager.getCache(cacheName) != null) {
                Objects.requireNonNull(cacheManager.getCache(cacheName)).clear();
                log.info("Cleared cache: ({})", cacheName);
            } else {
                log.error("Cache not found: ({})", cacheName);
            }
        });
    }

}
