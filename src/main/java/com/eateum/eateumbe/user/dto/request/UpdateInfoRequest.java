package com.eateum.eateumbe.user.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateInfoRequest {

    private String name;
    private String phone;
    //    private String profileImage; //프로필이미지 파일은 MultipartFile로만 처리

}
