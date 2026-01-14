package com.eateum.eateumbe.user.service.profile;

import com.eateum.eateumbe.global.error.ApiException;
import com.eateum.eateumbe.user.domain.User;
import com.eateum.eateumbe.user.dto.request.UpdateInfoRequest;
import com.eateum.eateumbe.user.dto.response.UserInfoResponse;
import com.eateum.eateumbe.user.repository.UserMapper;
import com.eateum.eateumbe.user.service.image.ProfileImageService;
import com.eateum.eateumbe.user.service.reader.UserReader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

/**
 * 프로필 관리
 * - 조회 / 수정
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserProfileServiceImpl implements UserProfileService {

    private final UserMapper userMapper;
    private final UserReader userReader;
    private final ProfileImageService profileImageService;

    /**
     * 프로필 조회
     */
    @Override
    public UserInfoResponse getUserInfo(String userId) {
        User user = userReader.getActiveUser(userId);

        return UserInfoResponse.builder()
                .email(user.getEmail())
                .name(user.getName())
                .phone(user.getPhone())
                .profileImage(profileImageService.resolve(user.getProfileImage())) //프로필 이미지가 있는지?
                .build();
    }

    /**
     * 프로필 수정
     */
    @Transactional
    @Override
    public void updateInfo(String userId, UpdateInfoRequest updateInfoRequest, MultipartFile profileImage) {
        User user = userReader.getActiveUser(userId);

        //이름 수정
        if(updateInfoRequest.getName() != null && !updateInfoRequest.getName().isBlank()) {
            user.setName(updateInfoRequest.getName());
        }

        //전화번호 수정
        if(updateInfoRequest.getPhone() != null && !updateInfoRequest.getPhone().isBlank()) {

            //기존과 다를 때만 중복 검사
            if(!updateInfoRequest.getPhone().equals(user.getPhone()) && userMapper.existsByPhone(updateInfoRequest.getPhone()) > 0) {
                throw new ApiException(HttpStatus.CONFLICT, "이미 사용 중인 전화번호입니다. 다시 확인해 주세요.");
            }

            user.setPhone(updateInfoRequest.getPhone());
        }

        //프로필 이미지 수정
        if(profileImage != null && !profileImage.isEmpty()) {
            //기존 이미지 삭제 (기본 이미지 제외)
            profileImageService.delete(user.getProfileImage());

            //새 이미지 업로드
            String newImageUrl = profileImageService.upload(profileImage);
            user.setProfileImage(newImageUrl);
        }

        userMapper.updateUserInfo(user);
    }

    /**
     * 프로필 이미지 삭제
     */
    @Transactional
    @Override
    public void deleteProfileImageOnly(String userId) {
        User user = userReader.getActiveUser(userId);

        profileImageService.delete(user.getProfileImage()); //기본 이미지라면 그냥 리턴
        user.setProfileImage(null); //아니라면 프로필 이미지 url을 null로

        userMapper.updateUserInfo(user); //수정
    }

}
