package com.eateum.eateumbe.user.service.auth;

import com.eateum.eateumbe.chat.memory.ChatMemoryRouter;
import com.eateum.eateumbe.global.error.ApiException;
import com.eateum.eateumbe.global.jwt.JwtProperties;
import com.eateum.eateumbe.global.jwt.JwtProvider;
import com.eateum.eateumbe.global.redis.RefreshTokenService;
import com.eateum.eateumbe.user.domain.User;
import com.eateum.eateumbe.user.dto.request.LoginRequest;
import com.eateum.eateumbe.user.dto.response.LoginResponse;
import com.eateum.eateumbe.user.repository.UserMapper;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * 인증 로직
 * - 로그인 / 토큰 재발급 / 로그아웃
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final RefreshTokenService refreshTokenService;
    private final JwtProperties jwtProperties;
    private final ChatMemoryRouter memoryRouter;

    /**
     * 로그인 시 비밀번호를 검증하고 JWT토큰을 발행
     */
    @Override
    public LoginResponse login(LoginRequest request, HttpServletResponse response) {

        //이메일로 사용자 조회
        User user = userMapper.findByEmail(request.getEmail());

        if(user == null){
            throw new ApiException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다.");
        }

        //비밀번호 검증
        if(!passwordEncoder.matches(request.getPassword(),user.getPassword())){
            throw new ApiException(HttpStatus.UNAUTHORIZED, "비밀번호가 일치하지 않습니다.");
        }

        //JWT payload 생성
        Map<String,Object> claims = new HashMap<>();
        claims.put("userId", user.getUserId());

        //토큰 생성
        String accessToken = jwtProvider.createAccessToken(claims);
        String refreshToken = jwtProvider.createRefreshToken(claims);

        //쿠키 갱신
        saveRefreshToken(user.getUserId(), refreshToken, response);

        //AccessToken만 반환
        return new LoginResponse(accessToken);
    }

    /**
     * 액세스토큰 재발행
     */
    @Override
    public LoginResponse reissue(String refreshToken, HttpServletResponse response) {

        if(refreshToken == null){
            throw new ApiException(HttpStatus.UNAUTHORIZED, "리프레시 토큰이 없습니다.");
        }

        //refreshToken 검증 + claims 추출
        Claims claims = jwtProvider.parseRefreshClaims(refreshToken);

        String userId = String.valueOf(claims.get("userId"));

        //Redis에 저장된 refreshToken 조회
        String savedRefreshToken = refreshTokenService.find(userId);

        log.info("refreshToken from cookie = {}", refreshToken);
        log.info("refreshToken from redis  = {}", savedRefreshToken);

        //Redis에 없거나 값이 다르면 실패
        if(savedRefreshToken == null || !savedRefreshToken.equals(refreshToken)){
            throw new ApiException(HttpStatus.UNAUTHORIZED, "리프레시 토큰이 유효하지 않습니다.");
        }

        Map<String, Object> newClaims = Map.of("userId", userId);

        //새 AccessToken 생성
        String newAccessToken = jwtProvider.createAccessToken(newClaims);

        //새 RefreshToken 생성
        String newRefreshToken = jwtProvider.createRefreshToken(newClaims);

        //쿠키 갱신
        saveRefreshToken(userId, newRefreshToken, response);

        //AccessToken만 반환
        return new LoginResponse(newAccessToken);
    }

    /**
     * RefreshToken을 Redis와 쿠키에 저장
     */
    private void saveRefreshToken(String userId, String refreshToken, HttpServletResponse response) {

        //Redis에 저장
        refreshTokenService.save(userId, refreshToken);

        ResponseCookie cookie = ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true)
                .secure(false) //localhost에서는 false
                .path("/user")
                .maxAge(Duration.ofSeconds(
                        jwtProperties.getRefreshToken().getExpireSeconds()
                ))
                .sameSite("Lax")
                .build();

        response.addHeader("Set-Cookie", cookie.toString());

    }

    /**
     * 로그아웃
     * Redis와 Cookie에서 RefreshToken 삭제
     */
    @Override
    public void logout(String refreshToken, HttpServletResponse response) {

        //쿠키가 없으면 그냥 쿠키 삭제만 실행
        if (refreshToken != null) {
            try {
                Claims claims = jwtProvider.parseRefreshClaims(refreshToken);
                String userId = String.valueOf(claims.get("userId"));

                //Redis에서 RefreshToken 삭제
                refreshTokenService.delete(userId);
                log.info("[Logout] refresh token deleted. userId={}", userId);

                //Redis에서 챗봇 대화 기록 삭제
                memoryRouter.getForMember().clear(userId);

            } catch (Exception e) {
                //만료 or 위조 토큰이어도 로그아웃은 성공 처리
                log.warn("[Logout] invalid refresh token");
            }
        }

        //RefreshTokenService 쿠키 제거
        ResponseCookie deleteCookie = ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .secure(false) // localhost에서는 false
                .path("/user")
                .maxAge(0)
                .sameSite("Lax")
                .build();

        response.addHeader("Set-Cookie", deleteCookie.toString());
    }

}
