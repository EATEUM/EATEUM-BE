package com.eateum.eateumbe.chat.summary;

import com.eateum.eateumbe.chat.memory.ChatMessage;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * ChatSummaryService 구현체
 * - 이전 대화를 요약하여 Redis에 저장
 * Prompt 길이 관리 목적
 */
@Service
public class ChatSummaryServiceImpl implements ChatSummaryService {

    @Override
    public String summarize(List<ChatMessage> messages) {

        if (messages == null || messages.isEmpty()) {
            return "";
        }

        //최근 사용자 대화 위주로 요약
        List<String> userMessages = messages.stream()
                .filter(message -> message.getRole() == ChatMessage.Role.USER)
                .map(ChatMessage::getContent)
                .limit(5)
                .collect(Collectors.toList());

        if (userMessages.isEmpty()) {
            return "";
        }

        return "이전 대화 요약: 사용자는 최근에 "
                + String.join(", ", userMessages)
                + " 등에 대해 질문했다.";
    }
}
