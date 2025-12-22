package com.eateum.eateumbe.chat.memory;

import java.util.List;

/**
 * 챗봇 대화 메모리 인터페이스
 */
public interface ChatMemory {

    //사용자별 대화 기록 조회
    List<ChatMessage> load(String userId);

    //사용자 대화 저장
    void save(String userId, ChatMessage message);

    //대화 기록 초기화
    void clear(String userId);
}
