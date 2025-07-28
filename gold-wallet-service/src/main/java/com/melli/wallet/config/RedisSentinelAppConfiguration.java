package com.melli.wallet.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisSentinelConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.util.Arrays;
import java.util.List;

/**
 * Redis Sentinel Configuration
 * This configuration is used when Redis Sentinel is enabled
 */
@Configuration
@ConditionalOnProperty(name = "redis.sentinel.enabled", havingValue = "true")
public class RedisSentinelAppConfiguration {

    @Value("${redis.sentinel.master}")
    private String masterName;

    @Value("${redis.sentinel.nodes}")
    private String sentinelNodes;

    @Value("${redis.sentinel.password:}")
    private String sentinelPassword;

    @Value("${redis.password:}")
    private String redisPassword;

    @Value("${redis.number:0}")
    private int database;

    @Bean
    @Primary
    public RedisConnectionFactory redisConnectionFactory() {
        // Parse sentinel nodes
        List<String> nodes = Arrays.asList(sentinelNodes.split(","));
        
        RedisSentinelConfiguration sentinelConfig = new RedisSentinelConfiguration()
                .master(masterName)
                .sentinel(nodes.getFirst().split(":")[0], Integer.parseInt(nodes.getFirst().split(":")[1]));
        
        // Add additional sentinel nodes if available
        for (int i = 1; i < nodes.size(); i++) {
            String[] node = nodes.get(i).split(":");
            sentinelConfig.sentinel(node[0], Integer.parseInt(node[1]));
        }
        
        // Set password if configured
        if (sentinelPassword != null && !sentinelPassword.isEmpty()) {
            sentinelConfig.setSentinelPassword(sentinelPassword);
        }
        
        if (redisPassword != null && !redisPassword.isEmpty()) {
            sentinelConfig.setPassword(redisPassword);
        }
        
        sentinelConfig.setDatabase(database);
        
        return new LettuceConnectionFactory(sentinelConfig);
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        
        // Set serializers
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());
        
        template.afterPropertiesSet();
        return template;
    }
} 