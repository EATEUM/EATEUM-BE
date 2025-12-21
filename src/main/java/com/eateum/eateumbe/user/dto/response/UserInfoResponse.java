package com.eateum.eateumbe.user.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class UserInfoResponse {
    private String email;
    private String name;
    private String phone;
    private String profileImage;
}
