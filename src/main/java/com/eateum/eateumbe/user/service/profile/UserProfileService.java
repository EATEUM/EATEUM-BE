package com.eateum.eateumbe.user.service.profile;

import com.eateum.eateumbe.user.dto.request.UpdateInfoRequest;
import com.eateum.eateumbe.user.dto.response.UserInfoResponse;
import org.springframework.web.multipart.MultipartFile;

public interface UserProfileService {

    //프로필 조회
    UserInfoResponse getUserInfo(String userId);

    //프로필 수정
    void updateInfo(String userId, UpdateInfoRequest updateInfoRequest, MultipartFile profileImage);

    //프로필 이미지 삭제
    void deleteProfileImageOnly(String userId);

}
