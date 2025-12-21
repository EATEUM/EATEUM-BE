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

    //전화번호 중복 확인
    int existsByPhone(String phone);

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

    //아이디 찾기
    User findIdByNameAndPhone(@Param("name") String name, @Param("phone") String phone);

    //비밀번호 찾기
    void resetPassword(@Param("userId") String userId, @Param("password") String password);

    //비밀번호 찾기 시 정보 조회
    User findByEmailAll(@Param("email") String email);

    //재활성화
    int reActive(@Param("email") String email, @Param("name")String name, @Param("phone") String phone);
}
