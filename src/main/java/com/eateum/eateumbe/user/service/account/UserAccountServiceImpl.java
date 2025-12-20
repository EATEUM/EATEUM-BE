package com.eateum.eateumbe.user.service.account;

import com.eateum.eateumbe.global.error.ApiException;
import com.eateum.eateumbe.global.redis.RefreshTokenService;
import com.eateum.eateumbe.user.domain.User;
import com.eateum.eateumbe.user.dto.request.PasswordChangeRequest;
import com.eateum.eateumbe.user.dto.request.SignupRequest;
import com.eateum.eateumbe.user.repository.UserMapper;
import com.eateum.eateumbe.user.service.image.ProfileImageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

/**
 * 계정 관리 로직
 * - 회원가입, 비밀번호 변경, 회원 탈퇴
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserAccountServiceImpl implements UserAccountService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenService refreshTokenService;
    private final ProfileImageService profileImageService;

    /**
     * 회원가입
     */
    @Override
    public void signup(SignupRequest signupRequest, MultipartFile profileImage) {

        //이메일 중복 체크
        if(userMapper.existsByEmail(signupRequest.getEmail()) > 0) {
            throw new ApiException(HttpStatus.CONFLICT, "이미 사용 중인 이메일입니다.");
        }

        //프로필 이미지 업로드
        String imageUrl = null;
        if(profileImage != null && !profileImage.isEmpty()) {
            imageUrl = profileImageService.upload(profileImage);
        }

        //User 생성
        User user = User.builder()
                .userId(UUID.randomUUID().toString())
                .email(signupRequest.getEmail())
                .password(passwordEncoder.encode(signupRequest.getPassword()))
                .name(signupRequest.getName())
                .profileImage(imageUrl)
                .build();

        userMapper.insertUser(user);
    }



    /**
     * 비밀번호 변경
     */
    @Override
    public void changePassword(String userId, PasswordChangeRequest passwordChangeRequest) {
        User user = userMapper.findByUserIdForPassword(userId);
        if (user == null) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "인증 정보가 유효하지 않습니다.");
        }

        //현재 비밀번호 일치 여부
        if(!passwordEncoder.matches(passwordChangeRequest.getCurrentPassword(), user.getPassword())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "현재 비밀번호가 일치하지 않습니다.");
        }

        //새 비밀번호
        if(!passwordChangeRequest.getNewPassword().equals(passwordChangeRequest.getConfirmPassword())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "새 비밀번호가 일치하지 않습니다.");
        }

        //비밀번호 암호화 후 변경하기
        String encodedNewPassword = passwordEncoder.encode(passwordChangeRequest.getNewPassword());
        userMapper.updatePassword(userId, encodedNewPassword);

        //RefreshToken제거로 강제 로그아웃!
        refreshTokenService.delete(userId);
    }

    /**
     * 비밀번호 확인
     */
    @Override
    public void checkPassword(String password) {
        //현재 로그인 사용자 ID 정보 받기
        String userId = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        //조회
        User user = userMapper.findByUserIdForPassword(userId);
        if (user == null) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "인증 정보가 유효하지 않습니다.");
        }

        //비밀번호 검증
        if(!passwordEncoder.matches(password, user.getPassword())) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "비밀번호가 일치하지 않습니다.");
        }

        //성공시에는 아무것도 하지 않아도 됨!
    }

    /**
     * 회원탈퇴
     */
    @Override
    public void withdraw(String userId) {
        int withdrawCount = userMapper.withdraw(userId);

        if(withdrawCount == 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "이미 탈퇴했거나 존재하지 않는 사용자입니다.");
        }

        //RefreshToken 삭제 (강제 로그아웃)
        refreshTokenService.delete(userId);
    }



}
