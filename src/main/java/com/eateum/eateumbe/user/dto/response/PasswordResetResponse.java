package com.eateum.eateumbe.user.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PasswordResetResponse {

    private String message;
    private boolean reactivated;

}
