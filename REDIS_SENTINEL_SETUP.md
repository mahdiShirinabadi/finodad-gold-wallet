# Redis Sentinel Setup Guide

## Overview
This guide explains how to configure your application to use Redis Sentinel for high availability and failover support.

## What is Redis Sentinel?
Redis Sentinel is a monitoring system that helps manage Redis instances. It provides:
- **Automatic failover**: When a master node fails, Sentinel automatically promotes a slave to master
- **Monitoring**: Continuously monitors Redis master and slave instances
- **Notification**: Alerts when Redis instances are not working properly

## Configuration Options

### Option 1: Single Redis Instance (Current Setup)
```properties
# application-dev.properties
redis.host=192.168.211.128
redis.port=6379
redis.password=
redis.number=9

spring.data.redis.host=${redis.host}
spring.data.redis.port=${redis.port}
spring.data.redis.password=${redis.password}
spring.data.redis.database=${redis.number}
```

### Option 2: Redis Sentinel (High Availability)
```properties
# application-sentinel.properties
redis.sentinel.enabled=true
redis.sentinel.master=mymaster
redis.sentinel.nodes=192.168.211.128:26379,192.168.211.129:26379,192.168.211.130:26379
redis.sentinel.password=
redis.password=
redis.number=9

spring.data.redis.sentinel.master=${redis.sentinel.master}
spring.data.redis.sentinel.nodes=${redis.sentinel.nodes}
spring.data.redis.sentinel.password=${redis.sentinel.password}
spring.data.redis.password=${redis.password}
spring.data.redis.database=${redis.number}
```

## How to Enable Redis Sentinel

### Step 1: Update Application Properties
1. **For Development**: Edit `application-dev.properties`
   ```properties
   # Comment out single Redis configuration
   # redis.host=192.168.211.128
   # redis.port=6379
   # redis.password=
   # redis.number=9
   
   # Enable Sentinel configuration
   redis.sentinel.enabled=true
   redis.sentinel.master=mymaster
   redis.sentinel.nodes=192.168.211.128:26379,192.168.211.129:26379,192.168.211.130:26379
   redis.sentinel.password=
   redis.password=
   redis.number=9
   
   # Comment out single Redis Spring configuration
   # spring.data.redis.host=${redis.host}
   # spring.data.redis.port=${redis.port}
   # spring.data.redis.password=${redis.password}
   # spring.data.redis.database=${redis.number}
   
   # Enable Sentinel Spring configuration
   spring.data.redis.sentinel.master=${redis.sentinel.master}
   spring.data.redis.sentinel.nodes=${redis.sentinel.nodes}
   spring.data.redis.sentinel.password=${redis.sentinel.password}
   spring.data.redis.password=${redis.password}
   spring.data.redis.database=${redis.number}
   ```

2. **For Production**: Use `application-sentinel.properties` profile
   ```bash
   java -jar your-app.jar --spring.profiles.active=sentinel
   ```

### Step 2: Redis Sentinel Configuration Files

#### Redis Sentinel Configuration (sentinel.conf)
```conf
# sentinel.conf
port 26379
sentinel monitor mymaster 192.168.211.128 6379 2
sentinel down-after-milliseconds mymaster 5000
sentinel failover-timeout mymaster 10000
sentinel parallel-syncs mymaster 1
```

#### Redis Master Configuration (redis.conf)
```conf
# redis.conf for master
bind 0.0.0.0
port 6379
requirepass your_password
```

#### Redis Slave Configuration (redis.conf)
```conf
# redis.conf for slaves
bind 0.0.0.0
port 6379
requirepass your_password
slaveof 192.168.211.128 6379
masterauth your_password
```

## Configuration Classes

### RedisSentinelConfiguration.java
- **Purpose**: Configures Redis Sentinel connection
- **Activation**: When `redis.sentinel.enabled=true`
- **Features**: 
  - Automatic failover support
  - Multiple sentinel nodes
  - Password authentication
  - Database selection

### RedisStandaloneConfig.java
- **Purpose**: Configures single Redis instance
- **Activation**: When `redis.sentinel.enabled=false` (default)
- **Features**:
  - Simple single instance connection
  - Standard Redis configuration

## Testing Redis Sentinel

### 1. Check Sentinel Status
```bash
redis-cli -h 192.168.211.128 -p 26379
> sentinel masters
> sentinel slaves mymaster
```

### 2. Test Failover
```bash
# Stop master Redis
redis-cli -h 192.168.211.128 -p 6379 shutdown

# Check sentinel logs
tail -f /var/log/redis/sentinel.log

# Verify new master
redis-cli -h 192.168.211.128 -p 26379
> sentinel masters
```

### 3. Application Health Check
```bash
# Check application logs
tail -f application.log

# Test Redis connection in application
curl http://localhost:8010/actuator/health
```

## Monitoring and Alerts

### Sentinel Commands
```bash
# Check sentinel info
redis-cli -h 192.168.211.128 -p 26379 info sentinel

# Check master info
redis-cli -h 192.168.211.128 -p 26379 sentinel masters

# Check slaves info
redis-cli -h 192.168.211.128 -p 26379 sentinel slaves mymaster
```

### Application Monitoring
- **Health Checks**: `/actuator/health`
- **Metrics**: `/actuator/metrics`
- **Redis Metrics**: `/actuator/metrics/redis.connections`

## Troubleshooting

### Common Issues

1. **Connection Refused**
   ```bash
   # Check if sentinel is running
   ps aux | grep redis-sentinel
   
   # Check sentinel port
   netstat -tlnp | grep 26379
   ```

2. **Authentication Failed**
   ```properties
   # Verify password configuration
   redis.sentinel.password=your_sentinel_password
   redis.password=your_redis_password
   ```

3. **Master Not Found**
   ```bash
   # Check sentinel configuration
   redis-cli -h 192.168.211.128 -p 26379 sentinel masters
   
   # Verify master name matches
   redis.sentinel.master=mymaster
   ```

### Log Analysis
```bash
# Application logs
tail -f logs/application.log | grep -i redis

# Sentinel logs
tail -f /var/log/redis/sentinel.log

# Redis logs
tail -f /var/log/redis/redis.log
```

## Performance Considerations

### Connection Pooling
```properties
# Optimize connection pool for Sentinel
spring.data.redis.lettuce.pool.max-active=8
spring.data.redis.lettuce.pool.max-idle=8
spring.data.redis.lettuce.pool.min-idle=0
spring.data.redis.lettuce.pool.max-wait=-1ms
```

### Timeout Configuration
```properties
# Configure timeouts for Sentinel
spring.data.redis.timeout=5000ms
spring.data.redis.lettuce.shutdown-timeout=100ms
```

## Security Best Practices

1. **Use Strong Passwords**
   ```properties
   redis.sentinel.password=strong_sentinel_password
   redis.password=strong_redis_password
   ```

2. **Network Security**
   ```bash
   # Configure firewall rules
   iptables -A INPUT -p tcp --dport 26379 -s trusted_ip -j ACCEPT
   iptables -A INPUT -p tcp --dport 6379 -s trusted_ip -j ACCEPT
   ```

3. **SSL/TLS Configuration**
   ```properties
   # Enable SSL for Redis (if supported)
   spring.data.redis.ssl=true
   spring.data.redis.ssl.bundle=redis
   ```

## Migration Guide

### From Single Redis to Sentinel

1. **Backup Current Data**
   ```bash
   redis-cli -h 192.168.211.128 -p 6379 BGSAVE
   ```

2. **Update Configuration**
   - Modify `application-dev.properties`
   - Enable sentinel configuration
   - Comment out single Redis configuration

3. **Test Configuration**
   - Start application with sentinel profile
   - Verify connection and functionality
   - Monitor logs for any issues

4. **Deploy to Production**
   - Use `application-sentinel.properties` profile
   - Monitor application health
   - Verify failover functionality

## Support and Maintenance

### Regular Maintenance Tasks
1. **Monitor Sentinel Logs**: Check for warnings or errors
2. **Verify Failover**: Test automatic failover periodically
3. **Update Configuration**: Keep sentinel configuration up to date
4. **Backup Data**: Regular Redis data backups
5. **Performance Monitoring**: Monitor Redis performance metrics

### Emergency Procedures
1. **Manual Failover**: If automatic failover fails
2. **Data Recovery**: Restore from backups if needed
3. **Configuration Rollback**: Revert to single Redis if necessary

## Conclusion

Redis Sentinel provides high availability and automatic failover for your Redis infrastructure. By following this guide, you can successfully configure your application to use Redis Sentinel and ensure your Redis data remains available even during node failures.

For additional support, refer to:
- [Redis Sentinel Documentation](https://redis.io/topics/sentinel)
- [Spring Data Redis Documentation](https://docs.spring.io/spring-data/redis/docs/current/reference/html/)
- [Application Logs](logs/application.log) 