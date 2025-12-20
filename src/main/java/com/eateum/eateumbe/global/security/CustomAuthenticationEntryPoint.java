package com.eateum.eateumbe.global.security;

import com.eateum.eateumbe.global.common.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * [인증 실패 진입점 (401 Unauthorized)] : AuthenticationException 발생 시 호출되는 클래스
 * 
 * - AccessToken 없음, AccessToken 만료/위조/형식 오류
 * - 클라이언트에게 401 Unauthorized 응답을 반환
 * - 보안 예외에 대한 응답 포맷을 통일하기 위해 사용
 * 
 * JwtVerificationFilter 또는 FilterSecurityInterceptor에서 인증실패가 발생하면
 * ExceptionTranslationFilter를 통해 이 클래스가 호출됨
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException
    ) throws IOException {

        String message = resolveMessage(authException);
        log.warn("인증 실패 : {}", message);

        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        response.getWriter().write(
                new ObjectMapper().writeValueAsString(
                        ApiResponse.fail(message)
                )
        );
    }

    /**
     * 인증 실패 사유에 따라 메시지 작성
     */
    private String resolveMessage(AuthenticationException e) {
        //토큰이 없는 경우
        if(e.getMessage() == null) {
            return "인증이 필요합니다.";
        }

        //JWT 검증 실패
        if("INVALID_TOKEN".equals(e.getMessage())) {
            return "유효하지 않는 토큰입니다.";
        }

        //기본값
        return "인증에 실패했습니다.";
    }


}
