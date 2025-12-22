package com.eateum.eateumbe.chat.memory;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 회원 / 비회원에 따라 사용할 ChatMemory 구현체 분기 전용 클래스
 */
@Component
@RequiredArgsConstructor
public class ChatMemoryRouter {

    private final RedisChatMemory redisChatMemory;
    private final InMemoryChatMemory inMemoryChatMemory;

    public ChatMemory getForMember() {
        return redisChatMemory;
    }

    public ChatMemory getForGuest() {
        return inMemoryChatMemory;
    }
}
