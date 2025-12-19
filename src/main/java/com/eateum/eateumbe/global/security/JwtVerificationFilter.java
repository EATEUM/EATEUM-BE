package com.eateum.eateumbe.global.security;

import com.eateum.eateumbe.global.error.ApiException;
import com.eateum.eateumbe.global.jwt.JwtProvider;
import com.eateum.eateumbe.global.util.AuthorizationExtractor;
import com.fasterxml.jackson.databind.ObjectMapper; // 1. JSON 변환기 추가
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;



/**
 * 로그인 제외 요청에서 Authorization: Bear {accessToken} 검사
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtVerificationFilter extends OncePerRequestFilter {

    private final JwtProvider provider;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        String token = AuthorizationExtractor.extractBearearToken(request);

        //토큰이 없다면 다음으로
        if(token == null){
            filterChain.doFilter(request, response);
            return;
        }

        try {
            //토큰이 있으면 진짜인지 검사, 성공시 payload정보(Claims)를 얻음
            Claims claims = provider.parseClaims(token);

            //토큰에 넣어둔 사용자 식별 정보 꺼내기
            String userId = String.valueOf(claims.get("userId"));

            //권한을 만듦 (일단 ROLE_USER로 고정)
            var authorites = List.of(new SimpleGrantedAuthority("ROLE_USER"));

            //이 요청은 로그인한 사용자 요청이라는 authentication 인증 객체 만듦
            //principal: 대표 신분값 - userId 넣는게 일반적으로 편함
            //credentials: 비밀번호 - JWT방식에서는 필요 없음
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userId, null, authorites);

            //추가 정보(요청 IP 등)도 authentication에 달아줄 수 있음 (선택사항)
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            //SecurityContextHolder = 이번 요청의 로그인 정보 보관함
            //인증 객체를 넣으면 Controller/Service에서 로그인한 사용자로 인식됨
            SecurityContextHolder.getContext().setAuthentication(authentication);

            //검증 끝! 다음 필터 or 컨트롤러로 요청 넘김
            filterChain.doFilter(request, response);

        } catch (ApiException e) {
            log.warn("JWT 인증 실패 (API Exception): {}", e.getMessage());
            setErrorResponse(response, e.getMessage());

        } catch (JwtException | IllegalArgumentException e) {
            log.warn("JWT 인증 실패 (Token Error): {}", e.getMessage());
            setErrorResponse(response, "유효하지 않은 토큰입니다.");

        } catch (Exception e) {
            log.error("JWT 필터 알 수 없는 오류", e);
            setErrorResponse(response, "인증 처리 중 알 수 없는 오류가 발생했습니다.");
        }

    }

    private void setErrorResponse(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        Map<String, Object> body = new HashMap<>();
        body.put("status", HttpStatus.UNAUTHORIZED.value());
        body.put("error", "Unauthorized");
        body.put("message", message);

        response.getWriter().write(objectMapper.writeValueAsString(body));
    }

    /**
     * 헤더에 AccessToken이 없어야 하는 API는 JWT 검증 필터 자체에서 제외
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();

        return path.startsWith("/user/login")
                || path.startsWith("/user/signup")
                || path.startsWith("/user/reissue")
                || path.startsWith("/user/logout")
                || path.startsWith("/v3/api-docs")
                || path.startsWith("/swagger-ui");
    }
}

