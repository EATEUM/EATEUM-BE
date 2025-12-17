package com.eateum.eateumbe.global.util;

import jakarta.servlet.http.HttpServletRequest;

public class AuthorizationExtractor {

    private static final String BEARER_PREFIX = "Bearer ";

    //유틸 클래스는 new 불가
    private AuthorizationExtractor(){}

    /**
     * Authorization 헤더에서 토큰 추출
     */
    public static String extractBearearToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if(header == null) return null;
        if(!header.startsWith(BEARER_PREFIX)) return null;
        return header.substring(BEARER_PREFIX.length());
    }
}
