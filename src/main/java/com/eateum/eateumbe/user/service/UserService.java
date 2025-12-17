package com.eateum.eateumbe.user.service;

import com.eateum.eateumbe.user.domain.User;
import com.eateum.eateumbe.user.dto.request.LoginRequest;
import com.eateum.eateumbe.user.dto.response.LoginResponse;
import jakarta.servlet.http.HttpServletRequest;

public interface UserService {

    //로그인
    LoginResponse login(LoginRequest request);
    
    //엑세스토큰 재발행
//    LoginResponse reissue(HttpServletRequest request);

}
