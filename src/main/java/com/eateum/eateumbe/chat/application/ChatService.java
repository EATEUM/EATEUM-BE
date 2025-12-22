package com.eateum.eateumbe.chat.application;

import com.eateum.eateumbe.chat.memory.ChatMessage;

import java.util.List;

/**
 * 챗봇 기능의 진입점 인터페이스
 * - 회원 / 비회원 챗봇 처리 흐름을 정의한다.
 */
public interface ChatService {

    //비회원
    String chatForGuest(String sessionId, String message);

    //ChatResponse
    public String chatForMember(String userId, String message);

    //비회원 히스토리 조회
    List<ChatMessage> getHistoryForGuest(String sessionId);

    //회원 히스토리 조회
    List<ChatMessage> getHistoryForMember(String userId);
}
