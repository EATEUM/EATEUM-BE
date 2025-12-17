package com.eateum.eateumbe.global.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RedisTemplate<String, String> redisTemplate;
    
    //Refresh Token 유효기간 - 14일
    private static final Duration REFRESH_TOKEN_TTL = Duration.ofDays(14);
    
    private static final String PREFIX = "refresh:";

    /**
     * refresh Token 저장
     * key: refresh:{userId}
     * value: refreshToken
     */
    public void save(String userId, String refreshToken) {
        redisTemplate.opsForValue()
                .set(PREFIX + userId, refreshToken);
    }

    /**
     * refreshToken 조회
     */
    public String find(String userId) {
        return redisTemplate.opsForValue()
                .get(PREFIX + userId);
    }

    /**
     * 로그아웃 시 Refresh Token 삭제
     */
    public void delete(String userId) {
        redisTemplate.delete(PREFIX + userId);
    }
}
