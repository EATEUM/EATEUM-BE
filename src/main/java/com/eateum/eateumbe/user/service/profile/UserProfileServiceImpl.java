package com.eateum.eateumbe.user.service.profile;

import com.eateum.eateumbe.user.domain.User;
import com.eateum.eateumbe.user.dto.request.UpdateInfoRequest;
import com.eateum.eateumbe.user.dto.response.UserInfoResponse;
import com.eateum.eateumbe.user.repository.UserMapper;
import com.eateum.eateumbe.user.service.image.ProfileImageService;
import com.eateum.eateumbe.user.service.reader.UserReader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
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
                .profileImage(profileImageService.resolve(user.getProfileImage())) //프로필 이미지가 있는지?
                .build();
    }

    /**
     * 프로필 수정
     */
    @Override
    public void updateInfo(String userId, UpdateInfoRequest updateInfoRequest, MultipartFile profileImage) {
        User user = userReader.getActiveUser(userId);

        //이름 수정
        if(updateInfoRequest.getName() != null && !updateInfoRequest.getName().isBlank()) {
            user.setName(updateInfoRequest.getName());
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
    @Override
    public void deleteProfileImageOnly(String userId) {
        User user = userReader.getActiveUser(userId);

        profileImageService.delete(user.getProfileImage()); //기본 이미지라면 그냥 리턴
        user.setProfileImage(null); //아니라면 프로필 이미지 url을 null로

        userMapper.updateUserInfo(user); //수정
    }

}
