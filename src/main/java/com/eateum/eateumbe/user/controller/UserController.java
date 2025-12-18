package com.eateum.eateumbe.user.controller;

import com.eateum.eateumbe.global.common.ApiResponse;
import com.eateum.eateumbe.user.dto.request.LoginRequest;
import com.eateum.eateumbe.user.dto.response.LoginResponse;
import com.eateum.eateumbe.user.dto.response.UserInfoResponse;
import com.eateum.eateumbe.user.service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(
            @RequestBody LoginRequest request,
            HttpServletResponse response){
        LoginResponse loginResponse = userService.login(request,response);
        return ApiResponse.success(loginResponse);
    }

    @PostMapping("/reissue")
    public ApiResponse<LoginResponse> reissue(
            @CookieValue("refreshToken")
            String refreshToken,
            HttpServletResponse response){
        LoginResponse loginResponse = userService.reissue(refreshToken,response);
        return ApiResponse.success(loginResponse);
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout(
            @CookieValue(value = "refreshToken", required = false) String refreshToken,
            HttpServletResponse response){
        userService.logout(refreshToken, response);
        return ApiResponse.success(null);
    }

    @GetMapping("/info")
    public ApiResponse<UserInfoResponse> getUserInfo(@AuthenticationPrincipal String userId) {
        return ApiResponse.success(userService.getUserInfo(userId));
    }

    //AccessToken 인증 테스트용
//    @GetMapping("/me")
//    public String me(@AuthenticationPrincipal String userId) {
//        return "내 userId는 " + userId + " 입니다.";
//    }

}
