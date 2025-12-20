package com.eateum.eateumbe.global.security;

import com.eateum.eateumbe.global.common.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * [인가 실패 처리기 (403 Forbidden)] : 인증은 되었으나 권한이 없는 경우 호출됨
 * 
 * - 로그인은 했지만 해당 API에 접근 권한이 없는 경우
 * - 클라이언트에게 403 Forbidden 응답을 반환
 * 
 * FilterSecurityInterceptor에서 AccessDeniedException 발생 시
 * ExceptionTranslationFilter를 통해 이 클래스가 호출됨
 */
@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException {
        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setContentType("application/json;charset=UTF-8");

        response.getWriter().write(
                new ObjectMapper().writeValueAsString(
                        ApiResponse.fail("접근 권한이 없습니다.")
                )
        );

    }
}
