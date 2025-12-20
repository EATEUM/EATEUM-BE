package com.eateum.eateumbe.user.repository;

import com.eateum.eateumbe.user.domain.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

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

    //비밀번호 변경
    void updatePassword(@Param("userId") String userId, @Param("password") String password);

    //비밀번호 변경을 위한 사용자 정보 조회용
    User findByUserIdForPassword(String userId);

    //탈퇴
    int withdraw(String userId);
}
