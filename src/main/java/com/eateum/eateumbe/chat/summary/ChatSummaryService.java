package com.eateum.eateumbe.chat.summary;

import com.eateum.eateumbe.chat.memory.ChatMessage;

import java.util.List;

/**
 * 챗봇 대화 요약 서비스
 * - 대화가 길어질 경우 요약 생성
 */
public interface ChatSummaryService {
    
    //대화 내용을 요약해서 한 줄로 반환
    String summarize(List<ChatMessage> messages);
    
}
