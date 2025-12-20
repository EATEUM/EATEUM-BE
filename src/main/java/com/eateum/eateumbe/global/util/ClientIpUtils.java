package com.eateum.eateumbe.global.util;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

public class ClientIpUtils {

    private ClientIpUtils() {}

    // 현재 요청하는 클라이언트의 ip 주소 반환하는 로직
    public static String getRemoteIP() {
        HttpServletRequest req = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();

        String ip = req.getHeader("X-Forwarded-For");

        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = req.getRemoteAddr();
        }

        // X-Forwarded-For는 여러 IP가 올 수 있음 즉, 첫 번째가 실제 IP
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }

        return ip;
    }
}