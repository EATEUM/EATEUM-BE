package com.eateum.eateumbe.user.repository;

import com.eateum.eateumbe.user.domain.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper {

    //로그인
    User findByEmail(String email);

    //프로필 조회
    User findByUserId(String userId);

    //이메일 중복 확인
    int existsByEmail(String email);

    //회원가입
    void insertUser(User user);

    //프로필 수정
    void updateUserInfo(User user);

}
