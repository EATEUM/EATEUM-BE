package com.eateum.eateumbe.user.service;

import com.eateum.eateumbe.global.jwt.JwtProvider;
import com.eateum.eateumbe.global.redis.RefreshTokenService;
import com.eateum.eateumbe.global.util.AuthorizationExtractor;
import com.eateum.eateumbe.user.domain.User;
import com.eateum.eateumbe.user.dto.request.LoginRequest;
import com.eateum.eateumbe.user.dto.response.LoginResponse;
import com.eateum.eateumbe.user.repository.UserMapper;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

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

    /**
     * 로그인 시 비밀번호를 검증하고 JWT토큰을 발행
     */
    @Override
    public LoginResponse login(LoginRequest request) {

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

        refreshTokenService.save(user.getUserId(), refreshToken);

        //응답
        return new LoginResponse(accessToken, refreshToken);
    }

//    /**
//     * 액세스토큰 재발행
//     */
//    @Override
//    public LoginResponse reissue(HttpServletRequest request) {
//
//        //Authorization 헤더에서 refreshToken 추출
//        String refreshToken = AuthorizationExtractor.extractBearearToken(request);
//
//        if(refreshToken == null){
//            throw new RuntimeException("REFRESH_TOKEN_MISSING");
//        }
//
//        //refreshToken 검증 + claims 추출
//        Claims claims = jwtProvider.parseClaims(refreshToken);
//
//        String userId = String.valueOf(claims.get("userId"));
//        String name = String.valueOf(claims.get("name"));
//
//        //Redis에 저장된 refreshToken 조회
//        String savedRefreshToken = refreshTokenService.find(userId);
//
//        //Redis에 없거나 값이 다르면 실패
//        if(savedRefreshToken == null || !savedRefreshToken.equals(refreshToken)){
//            throw new RuntimeException("REFRESH_TOKEN_INVALID");
//        }
//
//        //새 AccessToken 발급
//        Map<String, Object> newClaims = Map.of(
//                "userId", userId,
//                "name", name);
//
//        String newAccessToken = jwtProvider.createAccessToken(newClaims);
//
//        //RefreshToken은 그대로, AccessToken만 교체
//        return new LoginResponse(newAccessToken, refreshToken);
//    }

}
