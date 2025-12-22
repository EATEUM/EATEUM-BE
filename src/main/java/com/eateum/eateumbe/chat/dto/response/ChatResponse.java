package com.eateum.eateumbe.chat.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 챗봇 응답 DTO
 * - AI 응답 메시지
 * - 비회원 세션 ID (필요 시)
 */
@Getter
@AllArgsConstructor
public class ChatResponse {

    private String answer;

    @JsonInclude(JsonInclude.Include.NON_NULL) //null이면 JSON에 나가지 않음
    private String sessionId;

}
