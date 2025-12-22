package com.eateum.eateumbe.chat.memory;

import org.springframework.stereotype.Component;

import java.util.*;

/**
 * 인메모리 용 구현 클래스 (비회원용)
 * - 서버 재시작시 초기화
 * - 간단한 세션 유지용
 */
@Component
public class InMemoryChatMemory implements ChatMemory {

    private static final int MAX_MESSAGE = 20; //최근 대화 20개만 저장
    private final Map<String, Deque<ChatMessage>> store = new HashMap<>(); //userId별 대화 저장

    //사용자의 지금까지 대화 기록 가져오기
    @Override
    public List<ChatMessage> load(String userId) {
        //있으면 기존대화, 없으면 빈 대화 > 복사본으로 반환
        return new ArrayList<>(store.getOrDefault(userId, new ArrayDeque<>()));
    }

    //사용자의 대화 한 줄을 저장
    @Override
    public void save(String userId, ChatMessage message) {
        //있으면 기존 Deque, 없으면 새 Deque만들어 Map에 저장
        Deque<ChatMessage> messages = store.computeIfAbsent(userId, k -> new ArrayDeque<>());

        messages.addLast(message); //최신 대화는 맨 뒤

        while (messages.size() > MAX_MESSAGE) { //용량 초과하면
            messages.removeFirst(); //가장 오래된 메세지 제거
        }
    }
    
    //사용자의 모든 대화 기록 삭제
    @Override
    public void clear(String userId) {
        store.remove(userId);
    }
}
