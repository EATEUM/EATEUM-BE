package com.eateum.eateumbe.user.controller;

import com.eateum.eateumbe.global.common.ApiResponse;
import com.eateum.eateumbe.global.error.ApiException;
import com.eateum.eateumbe.user.dto.request.*;
import com.eateum.eateumbe.user.dto.response.FindIdResponse;
import com.eateum.eateumbe.user.dto.response.LoginResponse;
import com.eateum.eateumbe.user.dto.response.PasswordResetResponse;
import com.eateum.eateumbe.user.dto.response.UserInfoResponse;
import com.eateum.eateumbe.user.service.auth.AuthService;
import com.eateum.eateumbe.user.service.account.UserAccountService;
import com.eateum.eateumbe.user.service.profile.UserProfileService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final AuthService authService;
    private final UserProfileService  userProfileService;
    private final UserAccountService userAccountService;

    /**
     * 로그인
     */
    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(
            @RequestBody LoginRequest request,
            HttpServletResponse response){
        LoginResponse loginResponse = authService.login(request,response);
        return ApiResponse.success(loginResponse);
    }

    /**
     * 엑세스 토큰 재발급
     */
    @PostMapping("/reissue")
    public ApiResponse<LoginResponse> reissue(
            @CookieValue(value = "refreshToken", required = false) String refreshToken,
            HttpServletResponse response){
        LoginResponse loginResponse = authService.reissue(refreshToken,response);
        return ApiResponse.success(loginResponse);
    }

    /**
     * 로그아웃
     */
    @PostMapping("/logout")
    public ApiResponse<Void> logout(
            @CookieValue(value = "refreshToken", required = false) String refreshToken,
            HttpServletResponse response){
        authService.logout(refreshToken, response);
        return ApiResponse.success(null);
    }

    /**
     * 프로필 조회
     */
    @GetMapping("/info")
    public ApiResponse<UserInfoResponse> getUserInfo(@AuthenticationPrincipal String userId) {
        return ApiResponse.success(userProfileService.getUserInfo(userId));
    }

    /**
     * 회원가입
     */
    @PostMapping(value = "/signup", consumes = MediaType.MULTIPART_FORM_DATA_VALUE) //multipart/form-data 요청만 받음
    public ApiResponse<Void> signup(@Valid @RequestPart("signup") SignupRequest signupRequest, //part단위로 나누어서 JSON 파트를 받음
                                    @RequestPart(value = "profileImage", required = false) MultipartFile profileImage){ //파일 파트를 받음
        userAccountService.signup(signupRequest, profileImage);
        return ApiResponse.success(null);
    }

    /**
     * 프로필 수정
     */
    @PatchMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<Void> updateInfo(@AuthenticationPrincipal String userId,
                                        @RequestPart("update") UpdateInfoRequest updateInfoRequest,
                                        @RequestPart(value = "profileImage", required = false) MultipartFile profileImage) {
        userProfileService.updateInfo(userId, updateInfoRequest, profileImage);
        return ApiResponse.success(null);
    }

    /**
     * 프로필 이미지 삭제
     */
    @PatchMapping("/image")
    public ApiResponse<Void> deleteProfileImage(@AuthenticationPrincipal String userId) {

        log.info("userId = {}", userId);
        userProfileService.deleteProfileImageOnly(userId);
        return ApiResponse.success(null);
    }

    /**
     * 비밀번호 변경
     */
    @PostMapping("/password")
    public ApiResponse<Void> changePassword(@AuthenticationPrincipal String userId,
                                            @RequestBody PasswordChangeRequest passwordChangeRequest) {

                userAccountService.changePassword(userId, passwordChangeRequest);
                return ApiResponse.success(null);
    }

    /**
     * 비밀번호 재확인 (프로필 진입 전)
     */
    @PostMapping("/check-password")
    public ApiResponse<Void> passwordCheck(@RequestBody @Valid PasswordCheckRequest passwordCheckRequest) {
        userAccountService.checkPassword(passwordCheckRequest.getPassword());
        return ApiResponse.success(null);
    }

    /**
     * 회원 탈퇴
     */
    @PatchMapping("/withdraw")
    public ApiResponse<Void> withdraw(@AuthenticationPrincipal String userId) {
        userAccountService.withdraw(userId);
        return ApiResponse.success(null);
    }

    /**
     * 아이디 찾기
     */
    @PostMapping("/find-id")
    public ApiResponse<FindIdResponse> findId(@RequestBody @Valid FindIdRequest findIdRequest) {
        FindIdResponse findIdResponse = userAccountService.findId(findIdRequest);
        return ApiResponse.success(findIdResponse);
    }

    /**
     * 비밀번호 찾기 (=재설정)
     */
    @PostMapping("/find-password")
    public ApiResponse<PasswordResetResponse> findPassword(@RequestBody @Valid PasswordResetRequest passwordResetRequest) {
        PasswordResetResponse passwordResetResponse = userAccountService.resetPassword(passwordResetRequest);
        return ApiResponse.success(passwordResetResponse);
    }

    /**
     * 이메일 중복 확인
     */
    @GetMapping("check-email")
    public ApiResponse<Void> checkEmail(@RequestParam String email) {
        userAccountService.checkEmailDuplicate(email);
        return ApiResponse.success(null);
    }

    //AccessToken 인증 테스트용
//    @GetMapping("/me")
//    public String me(@AuthenticationPrincipal String userId) {
//        return "내 userId는 " + userId + " 입니다.";
//    }

}
