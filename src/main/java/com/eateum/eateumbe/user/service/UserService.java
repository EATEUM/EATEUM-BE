package com.eateum.eateumbe.user.service;

import com.eateum.eateumbe.user.dto.request.LoginRequest;
import com.eateum.eateumbe.user.dto.request.PasswordChangeRequest;
import com.eateum.eateumbe.user.dto.request.SignupRequest;
import com.eateum.eateumbe.user.dto.request.UpdateInfoRequest;
import com.eateum.eateumbe.user.dto.response.LoginResponse;
import com.eateum.eateumbe.user.dto.response.UserInfoResponse;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.multipart.MultipartFile;

public interface UserService {

    //로그인
    LoginResponse login(LoginRequest request, HttpServletResponse response);
    
    //엑세스토큰 재발행
    LoginResponse reissue(String refreshToken, HttpServletResponse response);

    //로그아웃
    void logout(String refreshToken, HttpServletResponse response);

    //프로필 조회
    UserInfoResponse getUserInfo(String userId);

    //회원가입
    void signup(SignupRequest signupRequest, MultipartFile profileImage);

    //프로필 수정
    void updateInfo(String userId, UpdateInfoRequest updateInfoRequest, MultipartFile profileImage);

    void deleteProfileImageOnly(String userId);

    void changePassword(String userId, PasswordChangeRequest passwordChangeRequest);
}
