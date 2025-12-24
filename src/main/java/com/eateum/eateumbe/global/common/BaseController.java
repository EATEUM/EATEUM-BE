package com.eateum.eateumbe.global.common;

import com.eateum.eateumbe.global.error.ApiException;
import org.springframework.http.HttpStatus;

public abstract class BaseController {

    protected String resolveUserId(String userId) {
        if (userId == null || userId.equals("anonymousUser")) {
            return "guest";
        }
        return userId;
    }

    /**
     * 인증이 필수인 API에서 사용
     * userId가 null이거나 anonymousUser인 경우 401 UNAUTHORIZED 반환
     */
    protected String requireAuth(String userId) {
        if (userId == null || userId.equals("anonymousUser")) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
        }
        return userId;
    }
}
