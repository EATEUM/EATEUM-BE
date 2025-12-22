package com.eateum.eateumbe.global.config;

import com.eateum.eateumbe.global.jwt.JwtProvider;
import com.eateum.eateumbe.global.security.CustomAccessDeniedHandler;
import com.eateum.eateumbe.global.security.CustomAuthenticationEntryPoint;
import com.eateum.eateumbe.global.security.JwtVerificationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.ExceptionTranslationFilter;
import org.springframework.web.cors.CorsConfigurationSource;


/**
 * [Spring Security 설정 클래스]
 * <p>
 * - JWT 기반 stateless 인증 방식 사용
 * - 세션 / CSRF 비활성화
 * - 인증/인가 예외 방식 통일
 * <p>
 * - JwtVerificationFilter 등록
 * - CustomAuthenticationEntryPoint (401)
 * - CustomAccessDeniedHandler (403)
 * - URL별 접근 권한 설정
 * <p>
 * 이 설정을 통해 인증/인가 흐름과 예외 처리 책임을 Security 계층에 위임
 */
@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    //보안 예외 핸들러 등록
    private final CustomAuthenticationEntryPoint authenticationEntryPoint;
    private final CustomAccessDeniedHandler accessDeniedHandler;

    @Bean
    public JwtVerificationFilter jwtVerificationFilter(JwtProvider jwtProvider) {
        return new JwtVerificationFilter(jwtProvider);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            CorsConfigurationSource corsConfigurationSource,
            JwtVerificationFilter jwtVerificationFilter
    ) throws Exception {

        http
                //JWT방식이기 때문에 세션 쓰지 않음
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                //REST API이기 때문에 disable
                .csrf(csrf -> csrf.disable())

                //CORS (CorsConfig와 연결)
                .cors(cors -> cors.configurationSource(corsConfigurationSource))

                //보안 예외 처리 통일
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler)
                )

                //url 접근 규칙
                .authorizeHttpRequests(auth -> auth
                        //preflight OPTIONS 허용
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // swagger
                        .requestMatchers(
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html"
                        ).permitAll()

                        //인증 없이 가능
                        .requestMatchers(HttpMethod.POST,
                                "/user/signup",
                                "/user/login",
                                "/user/reissue",
                                "/user/logout",
                                "/user/find/id",
                                "/user/find/password",
                                "/chat/guest",
                                "/recipes/recommend/ai"
                        ).permitAll()

                        .requestMatchers(HttpMethod.GET,
                                "user/email/check",
                                "/chat/guest/history"
                        ).permitAll()

                        //그 외는 전부 인증 필요
                        .requestMatchers("/user/**").authenticated()
                        .requestMatchers("/recipes/*/memo/**").authenticated()
                        .requestMatchers("/recipes/my/**").authenticated()
                        .requestMatchers("/chat/**").authenticated()

                        //나머지 API는 일단 열어두고 추후 수정
                        .anyRequest().permitAll()
                );

        // 필터 순서

        // JWT 검증 필터 먼저 실행
        http.addFilterBefore(
                jwtVerificationFilter,
                ExceptionTranslationFilter.class
        );

        return http.build();
    }

    //비밀번호 암호화 (회원가입/비밀번호 변경 시 사용)
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
