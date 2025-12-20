package com.eateum.eateumbe.user.service.reader;

import com.eateum.eateumbe.global.error.ApiException;
import com.eateum.eateumbe.user.domain.User;
import com.eateum.eateumbe.user.repository.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

/**
 * 활성 사용자 조회 공통
 */
@Component
@RequiredArgsConstructor
public class UserReader {

    private final UserMapper userMapper;

    //인증된 활성 사용자 조회
    public User getActiveUser(String  userId) {
        User user = userMapper.findByUserId(userId);
        if(user == null){
            throw new ApiException(HttpStatus.UNAUTHORIZED, "인증 정보가 올바르지 않습니다.");
        }

        return user;
    }
}
