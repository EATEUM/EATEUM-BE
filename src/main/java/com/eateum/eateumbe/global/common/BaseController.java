package com.eateum.eateumbe.global.common;

public abstract class BaseController {

    protected String resolveUserId(String userId) {
        if (userId == null || userId.equals("anonymousUser")) {
            return "guest";
        }
        return userId;
    }
}
