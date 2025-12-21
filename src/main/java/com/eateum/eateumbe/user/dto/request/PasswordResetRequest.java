package com.eateum.eateumbe.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class PasswordResetRequest {

    @NotBlank
    private String email;
    @NotBlank
    private String name;
    @NotBlank
    private String phone;
    @NotBlank
    private String newPassword;
    @NotBlank
    private String confirmPassword;

}
