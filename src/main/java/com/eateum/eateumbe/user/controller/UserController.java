package com.eateum.eateumbe.user.controller;

import com.eateum.eateumbe.user.dto.request.LoginRequest;
import com.eateum.eateumbe.user.dto.response.LoginResponse;
import com.eateum.eateumbe.user.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest request){
        return userService.login(request);
    }

//    @PostMapping("/reissue")
//    public LoginResponse reissue(HttpServletRequest request){
//        return userService.reissue(request);
//    }
//
//    @GetMapping("/me")
//    public String me(@AuthenticationPrincipal String userId) {
//        return "내 userId는 " + userId + " 입니다.";
//    }

}
