package com.eateum.eateumbe.user.repository;

import com.eateum.eateumbe.user.domain.User;
import com.eateum.eateumbe.user.service.UserService;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper {

    //로그인
    User findByEmail(String email);

    //프로필 조회
    User findByUserId(String userId);

}
