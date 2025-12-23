package com.eateum.eateumbe.user.service.account;

import com.eateum.eateumbe.fridges.domain.Fridge;
import com.eateum.eateumbe.fridges.repository.FridgeMapper;
import com.eateum.eateumbe.global.error.ApiException;
import com.eateum.eateumbe.global.redis.RefreshTokenService;
import com.eateum.eateumbe.user.domain.User;
import com.eateum.eateumbe.user.dto.request.*;
import com.eateum.eateumbe.user.dto.response.FindIdResponse;
import com.eateum.eateumbe.user.dto.response.PasswordResetResponse;
import com.eateum.eateumbe.user.repository.UserMapper;
import com.eateum.eateumbe.user.service.image.ProfileImageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
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
    private final FridgeMapper fridgeMapper;

    /**
     * 회원가입
     */
    @Override
    public void signup(SignupRequest signupRequest, MultipartFile profileImage) {

        //이메일 중복 체크
        if(userMapper.existsByEmail(signupRequest.getEmail()) > 0) {
            throw new ApiException(HttpStatus.CONFLICT, "이미 사용 중인 이메일입니다.");
        }

        //전화번호 중복 체크
        if(userMapper.existsByPhone(signupRequest.getPhone()) > 0) {
            throw new ApiException(HttpStatus.CONFLICT, "입력하신 전화번호로 이미 가입된 계정이 있습니다. 아이디 찾기를 통해 확인해주세요.");
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
                .phone(signupRequest.getPhone())
                .profileImage(imageUrl)
                .build();

        userMapper.insertUser(user);

        List<String> defaultNames = List.of("돼지고기", "스팸", "달걀", "김치", "라면", "양파", "파");
        List<Fridge> defaultItems = fridgeMapper.selectItemsByNames(defaultNames);
        List<Long> itemIds = defaultItems.stream().map(Fridge::getItemId).toList();

        fridgeMapper.addFridgeItems(user.getUserId(), itemIds);
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

    /**
     * 아이디(=이메일) 찾기
     */
    @Override
    public FindIdResponse findId(FindIdRequest findIdRequest) {

        User user = userMapper.findIdByNameAndPhone(findIdRequest.getName(), findIdRequest.getPhone());

        //아예 존재하지 않는다면
        if (user == null) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "입력한 정보와 일치하는 사용자를 찾을 수 없습니다.");
        }

        //존재하는 사용자라면
        return new FindIdResponse(user.getEmail(), user.getIsActive() == 1);
    }

    /**
     * 비밀번호 재설정
     */
    @Override
    public PasswordResetResponse resetPassword(PasswordResetRequest passwordResetRequest) {
        
        User user = userMapper.findByEmailAll(passwordResetRequest.getEmail());
        if (user == null) {
            throw new ApiException(HttpStatus.NOT_FOUND, "존재하지 않는 사용자입니다.");
        }
        
        //본인 검증
        if (!user.getName().equals(passwordResetRequest.getName())
                || !user.getPhone().equals(passwordResetRequest.getPhone())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "사용자 정보가 일치하지 않습니다.");
        }

        //비밀번호 확인
        if(!passwordResetRequest.getNewPassword().equals(passwordResetRequest.getConfirmPassword())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "비밀번호가 일치하지 않습니다.");
        }
        
        //비밀번호 변경
        userMapper.resetPassword(user.getUserId(), passwordEncoder.encode(passwordResetRequest.getNewPassword()));

        //탈퇴 사용자면 재활성화
        if(user.getIsActive() == 0) {
            userMapper.reActive(user.getEmail(), user.getName(), user.getPhone());
            return new PasswordResetResponse(
                    "탈퇴한 계정이 재활성화되었습니다. 새 비밀번호로 로그인해주세요.",
                    true);
        }

        return new PasswordResetResponse(
                "비밀번호가 변경되었습니다. 다시 로그인해주세요.",
                false
        );
    }

    @Override
    public void checkEmailDuplicate(String email) {
        if(userMapper.existsByEmail(email) > 0) {
            throw new ApiException(HttpStatus.CONFLICT, "이미 사용 중인 이메일입니다.");
        }
    }
}
