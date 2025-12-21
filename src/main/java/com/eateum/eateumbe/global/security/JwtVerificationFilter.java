package com.eateum.eateumbe.global.security;

import com.eateum.eateumbe.global.jwt.JwtProvider;
import com.eateum.eateumbe.global.util.AuthorizationExtractor;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;



/**
 * [JWT 인증 필터] : Authorization 헤더의 AccessToken을 검사하는 필터
 *
 * - 토큰이 존재하면 : JWT 파싱 및 유효성 검증 > 성공 시 SecurityContext에 인증 정보 저장
 * - 토큰이 없으면 : 인증 없이 다음 필터로 요청 전달
 * - 토큰이 유효하지 않으면 : AuthenticationException을 던지고 직접 응답은 만들지 않음
 *   > 이후 ExceptionTranslationFilter에서 401 응답 처리됨
 *
 * 이 필터는 검증만 담당하며 HTTP 응답을 생성하지 않음
 */
@Slf4j
@RequiredArgsConstructor
public class JwtVerificationFilter extends OncePerRequestFilter {

    private final JwtProvider provider;

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

        } catch (JwtException | IllegalArgumentException e) {
            //토큰이 이상함 > 인증된 사용자 요청이 아님 > 로그인 정보를 비움 > 인증 실패 예외를 Security에게 넘김
            log.warn("JWT 인증 실패 : {}", e.getMessage());
            SecurityContextHolder.clearContext();
            throw new BadCredentialsException("INVALID_TOKEN");
        }
    }

    /**
     * 헤더에 AccessToken이 없어야 하는 API는 JWT 검증 필터 자체에서 제외
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        //OPTIONS 요청은 JWT 필터 제외
        if("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String path = request.getRequestURI();

        return path.startsWith("/user/login")
                || path.startsWith("/user/signup")
                || path.startsWith("/user/reissue")
                || path.startsWith("/user/logout")
                || path.startsWith("/user/find-id")
                || path.startsWith("/user/find-password")
                || path.startsWith("/user/check-email")
                || path.startsWith("/v3/api-docs")
                || path.startsWith("/swagger-ui");
    }
}

