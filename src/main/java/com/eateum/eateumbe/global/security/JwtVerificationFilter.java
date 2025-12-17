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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * 로그인 제외 요청에서 Authorization: Bear {accessToken} 검사
 */
@Component
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
            String name = String.valueOf(claims.get("name"));

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

        } catch (JwtException e) {

            //토큰이 이상 or 만료이면 401(인증 실패)
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");

            e.printStackTrace();

            //Vue가 이해하기 쉬운 메시지 형태로 내려줌
            response.getWriter().write("""
                 {"message":"TOKEN_ERROR"}
            """);
        }

    }

}

