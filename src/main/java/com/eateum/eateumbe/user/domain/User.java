package com.eateum.eateumbe.user.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Date;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    private String userId; //UUID
    private String email;
    private String password;
    private String name;
    private String role; //USER
    private int isActive; //1 = 활성, 0 = 비활성
    private String profileImage;
    private Date createdAt;
    private Date updateAt;

}
