package com.eateum.eateumbe.user.service.account;

import com.eateum.eateumbe.user.dto.request.PasswordChangeRequest;
import com.eateum.eateumbe.user.dto.request.SignupRequest;
import jakarta.validation.constraints.NotBlank;
import org.springframework.web.multipart.MultipartFile;

public interface UserAccountService {

    //회원가입
    void signup(SignupRequest signupRequest, MultipartFile profileImage);

    //비밀번호 변경
    void changePassword(String userId, PasswordChangeRequest passwordChangeRequest);

    //비밀번호 확인
    void checkPassword(String password);

    //탈퇴
    void withdraw(String userId);
}
