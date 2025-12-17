package com.eateum.eateumbe.global.redis;

import com.eateum.eateumbe.global.jwt.JwtProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RedisTemplate<String, String> redisTemplate;
    private final JwtProperties jwtProperties;
    
    private static final String PREFIX = "refresh:";

    /**
     * refresh Token 저장
     * key: refresh:{userId}
     * value: refreshToken
     */
    public void save(String userId, String refreshToken) {
        redisTemplate.opsForValue()
                .set(PREFIX + userId,
                        refreshToken,
                        Duration.ofSeconds(jwtProperties.getRefreshToken().getExpireSeconds())
                );
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
