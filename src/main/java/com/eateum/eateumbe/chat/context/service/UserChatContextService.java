package com.eateum.eateumbe.chat.context.service;

import com.eateum.eateumbe.chat.context.dto.UserChatContext;

/**
 * 회원 챗봇용 사용자 컨텍스트 조회 서비스
 * - DB에서 챗봇에 필요한 사용자 정보만 선별 조회
 */
public interface UserChatContextService {

    UserChatContext load(String userId);
    
}
