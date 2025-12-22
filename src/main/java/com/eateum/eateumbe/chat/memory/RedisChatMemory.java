package com.eateum.eateumbe.chat.memory;

import com.eateum.eateumbe.chat.summary.ChatSummaryService;
import com.eateum.eateumbe.global.error.ApiException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * Redis 용 구현 클래스 (회원)
 * - TTL 기반
 * - 최근 대화 개수 제한
 * - 요약 저장 기능
 */
@Component
@RequiredArgsConstructor
public class RedisChatMemory implements ChatMemory {

    private static final int MAX_MESSAGES = 20;
    private static final Duration TTL = Duration.ofMinutes(30);

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper; //JSON <> ChatMessage 객체
    private final ChatSummaryService chatSummaryService; //과거 내용 요약

    @Override
    public List<ChatMessage> load(String userId) {
        String key = generateKey(userId);

        List<String> values = redisTemplate.opsForList().range(key, 0, -1); //Redis 리스트 전체 조회

        if(values == null) {
            return List.of();
        }

        List<ChatMessage> messages = new ArrayList<>();
        for(String value : values) {
            messages.add(deserialize(value)); //JSON > ChatMessage로 변환
        }

        return messages;

    }

    @Override
    public void save(String userId, ChatMessage message) {
        String key = generateKey(userId);

        redisTemplate.opsForList().rightPush(key, serialize(message)); //최신 메시지를 맨 뒤에 추가

        Long size = redisTemplate.opsForList().size(key);

        if(size != null && size >= MAX_MESSAGES) {
            //기존 대화 로드
            List<ChatMessage> history = load(userId);
            //요약 생성
            String summary = chatSummaryService.summarize(history);
            //요약 저장
            saveSummary(userId, summary);

            //최근 대화만 남김
            redisTemplate.opsForList().trim(key, -10, -1);
        }
        //TTL 갱신
        redisTemplate.expire(key, TTL); //대화가 이어질 때마다 TTL 갱신 > 최근 30분간 대화한 사용자만 기억
    }

    @Override
    public void clear(String userId) {
        redisTemplate.delete(generateKey(userId)); //Redis에서 키 자체 제거
    }

    private String generateKey(String userId) {
        return "chat:memory:" + userId; //chat:memory:{userId}로 확인
    }

    private String serialize(ChatMessage message) {
        try {
            return objectMapper.writeValueAsString(message);
        } catch (JsonProcessingException e) {
            throw new ApiException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "대화 기록을 저장하는 중 오류가 발생했습니다."
            );
        }
    }

    //요약 조회 - prompt 만들 때 사용
    public String loadSummary(String userId) {
        String key = "chat:summary:" + userId;
        return redisTemplate.opsForValue().get(key);
    }

    //요약 저장
    private void saveSummary(String userId, String summary) {
        String key = "chat:summary:" + userId;

        redisTemplate.opsForValue().set(key, summary, Duration.ofDays(1)); //요약은 좀 더 오래 유지한다.
    }

    private ChatMessage deserialize(String value) {
        try {
            return objectMapper.readValue(value, ChatMessage.class);
        } catch (JsonProcessingException e) {
            throw new ApiException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "대화 기록을 불러오는 중 오류가 발생했습니다."
            );
        }
    }

}
