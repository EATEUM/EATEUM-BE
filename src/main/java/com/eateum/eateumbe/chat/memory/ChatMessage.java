package com.eateum.eateumbe.chat.memory;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 챗봇 대화 메시지 단위 객체
 * - USER / ASSISTANT 역할 구분
 * - 메모리 저장용
 */
@Getter
@AllArgsConstructor
public class ChatMessage {

    private Role role;
    private String content;

    public enum Role {
        USER,
        ASSISTANT,
        SYSTEM
    }
}
