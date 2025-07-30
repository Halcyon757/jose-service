package ru.t1.homework.jose_service.security;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class TokenBlacklistService {

    private final RedisTemplate<String, String> redis;
    @Value("${jwt.access-token.ttl-seconds}")
    private long ttlSeconds;

    public void revoke(String jti) {
        redis.opsForValue().set(jti, "", ttlSeconds, TimeUnit.SECONDS);
    }

    public boolean isRevoked(String jti) {
        return Boolean.TRUE.equals(redis.hasKey(jti));
    }
}
