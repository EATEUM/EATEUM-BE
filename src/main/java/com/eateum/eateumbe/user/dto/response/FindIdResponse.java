package com.eateum.eateumbe.user.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class FindIdResponse {

    private String email;
    private boolean active;

}
