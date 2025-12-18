package com.eateum.eateumbe.global.jwt;

import com.eateum.eateumbe.global.error.ApiException;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class JwtProvider {

    private final JwtProperties jwtProperties;
    private Key key;

    /**
     * 서버 시작 시 secret으로 서명 키 생성
     */
    @PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(
                jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8)
        );             
    }

    /**
     * Access Token 생성
     */
    public String createAccessToken(Map<String, Object> claims) {
        return createToken(
                claims,
                jwtProperties.getAccessToken().getExpireSeconds() * 1000
        );
    }

    /**
     * Refresh Token 생성
     */
    public String createRefreshToken(Map<String, Object> claims) {
        return createToken(
                claims,
                jwtProperties.getRefreshToken().getExpireSeconds() * 1000
        );
    }

    /**
     * JWT 공통 생성 로직
     */
    private String createToken(Map<String, Object> claims, long expireMillis) {
        Date now =  new Date();
        Date expiry = new Date(now.getTime() + expireMillis);

        System.out.println("===== JWT CREATE DEBUG =====");
        System.out.println("issuer = " + jwtProperties.getIssuer());
        System.out.println("claims = " + claims);
        System.out.println("issuedAt = " + now);
        System.out.println("expiry = " + expiry);
        System.out.println("============================");

        return Jwts.builder()
                .setClaims(claims)
                .setIssuer(jwtProperties.getIssuer())
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * AccessToken 검증 + Claim 반환 (필터용)
     */
    public Claims parseClaims(String token) {

        try {
            return Jwts.parserBuilder()
                    .setSigningKey(key)
                    .requireIssuer(jwtProperties.getIssuer())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) { //엑세스 토큰 만료
            throw new ApiException(HttpStatus.UNAUTHORIZED, "엑세스 토큰이 만료되었습니다.");
        } catch (JwtException e) { //위조,변조,형식 오류
            throw new ApiException(HttpStatus.UNAUTHORIZED, "엑세스 토큰이 유효하지 않습니다.");
        }
    }

    /**
     * RefreshToken 전용 검증 (Service용)
     */
    public Claims parseRefreshClaims(String refreshToken) {
        try{
            return Jwts.parserBuilder()
                    .setSigningKey(key)
                    .requireIssuer(jwtProperties.getIssuer())
                    .build()
                    .parseClaimsJws(refreshToken)
                    .getBody();
        } catch (ExpiredJwtException e) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "리프레시 토큰이 만료되었습니다.");
        } catch (JwtException e) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "리프레시 토큰이 유효하지 않습니다.");
        }
    }
}
