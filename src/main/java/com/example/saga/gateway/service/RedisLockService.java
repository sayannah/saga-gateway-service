package com.example.saga.gateway.service;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.UUID;

@Component
public class RedisLockService {

    private final StringRedisTemplate redis;
    private final String instanceId = UUID.randomUUID().toString();
    private static final String LOCK_PREFIX = "lock:tx:";
    private static final long LOCK_TTL_MS = 10_000;

    public RedisLockService(StringRedisTemplate redis) {
        this.redis = redis;
    }

    public boolean tryLock(String txId) {
        String key = LOCK_PREFIX + txId;
        String value = instanceId + ":" + System.currentTimeMillis();
        Boolean ok = redis.opsForValue().setIfAbsent(
                key,
                value,
                Duration.ofMillis(LOCK_TTL_MS)
        );
        return Boolean.TRUE.equals(ok);
    }

    public void unlock(String txId) {
        String key = LOCK_PREFIX + txId;
        redis.delete(key);
    }
}
