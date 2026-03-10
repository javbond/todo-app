package com.clarity.auth.infrastructure.token;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * Redis-backed token blacklist service placeholder.
 * Full implementation in Sprint 2.
 */
@Service
public class RedisTokenBlacklistService {

    private static final String KEY_PREFIX = "token:blacklist:";

    private final RedisTemplate<String, Object> redisTemplate;

    public RedisTokenBlacklistService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void blacklist(String jti, Duration ttl) {
        // TODO: Implement blacklisting in Sprint 2
    }

    public boolean isBlacklisted(String jti) {
        // TODO: Implement check in Sprint 2
        return false;
    }
}
