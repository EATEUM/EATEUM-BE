package com.eateum.eateumbe.user.service;

import com.eateum.eateumbe.global.jwt.JwtProperties;
import com.eateum.eateumbe.global.jwt.JwtProvider;
import com.eateum.eateumbe.global.redis.RefreshTokenService;
import com.eateum.eateumbe.global.util.AuthorizationExtractor;
import com.eateum.eateumbe.user.domain.User;
import com.eateum.eateumbe.user.dto.request.LoginRequest;
import com.eateum.eateumbe.user.dto.response.LoginResponse;
import com.eateum.eateumbe.user.repository.UserMapper;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseCookie;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final RefreshTokenService refreshTokenService;
    private final JwtProperties jwtProperties;

    /**
     * 로그인 시 비밀번호를 검증하고 JWT토큰을 발행
     */
    @Override
    public LoginResponse login(LoginRequest request, HttpServletResponse response) {

        //이메일로 사용자 조회
        User user = userMapper.findByEmail(request.getEmail());

        if(user == null){
            throw new RuntimeException("USER_NOT_FOUND");
        }

        //비밀번호 검증
        if(!passwordEncoder.matches(request.getPassword(),user.getPassword())){
            throw new RuntimeException("PASSWORD_NOT_MATCH");
        }

        //JWT payload 생성
        Map<String,Object> claims = new HashMap<>();
        claims.put("userId", user.getUserId());
        claims.put("name", user.getName());

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
            throw new RuntimeException("REFRESH_TOKEN_MISSING");
        }

        //refreshToken 검증 + claims 추출
        Claims claims = jwtProvider.parseRefreshClaims(refreshToken);

        String userId = String.valueOf(claims.get("userId"));
        String name = String.valueOf(claims.get("name"));

        //Redis에 저장된 refreshToken 조회
        String savedRefreshToken = refreshTokenService.find(userId);

        log.info("refreshToken from cookie = {}", refreshToken);
        log.info("refreshToken from redis  = {}", savedRefreshToken);

        //Redis에 없거나 값이 다르면 실패
        if(savedRefreshToken == null || !savedRefreshToken.equals(refreshToken)){
            throw new RuntimeException("REFRESH_TOKEN_INVALID");
        }

        Map<String, Object> newClaims = Map.of(
                "userId", userId,
                "name", name);

        //새 AccessToken 생성
        String newAccessToken = jwtProvider.createAccessToken(newClaims);

        //새 RefreshToken 생성
        String newRefreshToken = jwtProvider.createRefreshToken(newClaims);

        //쿠키 갱신
        saveRefreshToken(userId, newRefreshToken, response);

        //AccessToken만 반환
        return new LoginResponse(newAccessToken);
    }

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

}
