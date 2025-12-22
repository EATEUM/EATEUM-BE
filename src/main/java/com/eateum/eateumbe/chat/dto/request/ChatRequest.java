package com.eateum.eateumbe.chat.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 챗봇 요청 DTO (사용자 입력 메시지)
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ChatRequest {

    private String message;

}
