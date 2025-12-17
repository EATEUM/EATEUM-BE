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

    private String userId;
    private String email;
    private String password;
    private String name;
    private String role;
    private int isActive;
    private String profileImage;
    private Date createdAt;
    private Date updateAt;

}
