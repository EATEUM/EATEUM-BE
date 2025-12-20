package com.eateum.eateumbe.user.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PasswordChangeRequest {

    private String currentPassword;
    private String newPassword;
    private String confirmPassword;

}
