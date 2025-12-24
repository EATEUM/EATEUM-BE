package com.eateum.eateumbe.chat.controller;

import com.eateum.eateumbe.chat.dto.request.ChatRequest;
import com.eateum.eateumbe.chat.dto.response.ChatResponse;
import com.eateum.eateumbe.chat.application.ChatService;
import com.eateum.eateumbe.chat.memory.ChatMessage;
import com.eateum.eateumbe.global.common.ApiResponse;
import com.eateum.eateumbe.global.common.BaseController;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * 챗봇 API 컨트롤러
 * - 비회원 챗봇 (세션 기반)
 * - 회원 챗봇 (JWT + Redis 메모리)
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/chat")
public class ChatController extends BaseController {

    private final ChatService chatService;

    // 비회원 챗봇 - sessionId 있으면 대화 유지, 없으면 서버에서 생성
    @PostMapping("/guest")
    public ChatResponse chatForGuest(
            @RequestHeader(value = "X-Chat-Session-Id", required = false) String sessionId, // 세션ID 하나 받기
            @RequestBody ChatRequest chatRequest) {
        String actualSessionId = (sessionId != null) ? sessionId : UUID.randomUUID().toString();
        String answer = chatService.chatForGuest(actualSessionId, chatRequest.getMessage());
        return new ChatResponse(answer, actualSessionId);
    }

    // 회원 챗봇 - Redis 기반 기억
    @PostMapping("/member")
    public ChatResponse chatForMember(
            @AuthenticationPrincipal String userId,
            @RequestBody ChatRequest chatRequest) {
        String safeUserId = requireAuth(userId);
        String answer = chatService.chatForMember(safeUserId, chatRequest.getMessage());
        return new ChatResponse(answer, null);
    }

    // 비회원 히스토리 조회
    @GetMapping("/guest/history")
    public ApiResponse<List<ChatMessage>> getGuestHistory(@RequestHeader("X-Chat-Session-Id") String sessionId) {
        return ApiResponse.success(chatService.getHistoryForGuest(sessionId));
    }

    // 회원 히스토리 조회
    @GetMapping("/member/history")
    public ApiResponse<List<ChatMessage>> getMemberHistory(@AuthenticationPrincipal String userId) {
        String safeUserId = requireAuth(userId);
        return ApiResponse.success(chatService.getHistoryForMember(safeUserId));
    }

}
