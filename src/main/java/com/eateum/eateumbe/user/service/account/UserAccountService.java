package com.eateum.eateumbe.user.service.account;

import com.eateum.eateumbe.user.dto.request.*;
import com.eateum.eateumbe.user.dto.response.FindIdResponse;
import com.eateum.eateumbe.user.dto.response.PasswordResetResponse;
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

    //아이디 찾기
    FindIdResponse findId(FindIdRequest findIdRequest);

    //비밀번호 재설정
    PasswordResetResponse resetPassword(PasswordResetRequest passwordResetRequest);

    //이메일 중복 확인
    void checkEmailDuplicate(String email);
}
