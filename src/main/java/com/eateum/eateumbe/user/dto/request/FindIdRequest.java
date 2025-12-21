package com.eateum.eateumbe.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class FindIdRequest {

    @NotBlank
    private String name;
    @NotBlank
    private String phone;

}
