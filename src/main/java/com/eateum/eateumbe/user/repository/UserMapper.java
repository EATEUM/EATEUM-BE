package com.eateum.eateumbe.user.repository;

import com.eateum.eateumbe.user.domain.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper {

    //로그인
    User findByEmail(String email);

}
