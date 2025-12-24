package com.eateum.eateumbe.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
public class CorsConfig {

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        //Vue 개발 서버
        //config.setAllowedOrigins(List.of("http://localhost:5173"));
        config.setAllowedOriginPatterns(List.of("*"));

        //허용할 HTTP 메서드
        config.setAllowedMethods(List.of(
                "GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"
        ));

        //허용할 헤더
        config.setAllowedHeaders(List.of("*"));

        //Authorization 헤더 허용
        config.setExposedHeaders(List.of("Authorization"));

        //쿠키/인증정보 허용 여부
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
