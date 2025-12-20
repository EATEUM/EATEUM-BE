package com.eateum.eateumbe.user.service.auth;

import com.eateum.eateumbe.user.dto.request.LoginRequest;
import com.eateum.eateumbe.user.dto.response.LoginResponse;
import jakarta.servlet.http.HttpServletResponse;

public interface AuthService {

    //로그인
    LoginResponse login(LoginRequest request, HttpServletResponse response);

    //엑세스토큰 재발행
    LoginResponse reissue(String refreshToken, HttpServletResponse response);

    //로그아웃
    void logout(String refreshToken, HttpServletResponse response);

}
