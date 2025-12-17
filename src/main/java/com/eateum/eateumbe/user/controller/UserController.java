package com.eateum.eateumbe.user.controller;

import com.eateum.eateumbe.user.dto.request.LoginRequest;
import com.eateum.eateumbe.user.dto.response.LoginResponse;
import com.eateum.eateumbe.user.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
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
    public LoginResponse login(@RequestBody LoginRequest request, HttpServletResponse response){
        return userService.login(request, response);
    }

//    @PostMapping("/reissue")
//    public LoginResponse reissue(@CookieValue("refreshToken") String refreshToken, HttpServletResponse response){
//        return userService.reissue(refreshToken, response);
//    }
//
//    @GetMapping("/me")
//    public String me(@AuthenticationPrincipal String userId) {
//        return "내 userId는 " + userId + " 입니다.";
//    }

}
