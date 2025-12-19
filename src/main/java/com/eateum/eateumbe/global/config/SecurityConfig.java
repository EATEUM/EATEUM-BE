package com.eateum.eateumbe.global.config;

import com.eateum.eateumbe.global.security.JwtVerificationFilter;
import com.eateum.eateumbe.global.security.SecurityExceptionFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;


@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtVerificationFilter jwtVerificationFilter;

    //예외처리 필터
    private final SecurityExceptionFilter securityExceptionFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, CorsConfigurationSource corsConfigurationSource) throws Exception {

        http
                //JWT방식이기 때문에 세션 쓰지 않음
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                //REST API이기 때문에 disable
                .csrf(csrf -> csrf.disable())

                //CORS (CorsConfig와 연결)
                .cors(cors -> cors.configurationSource(corsConfigurationSource))

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
                        .requestMatchers(HttpMethod.POST, "/user/signup").permitAll()
                        .requestMatchers(HttpMethod.POST, "/user/login").permitAll()
                        .requestMatchers(HttpMethod.POST, "/user/reissue").permitAll()
                        .requestMatchers(HttpMethod.POST, "/user/logout").permitAll()

                        //그 외 user는 전부 인증 필요
                        .requestMatchers("/user/**").authenticated()
                        .requestMatchers("/recipes/*/memo/**").authenticated()
                        .requestMatchers("/recipes/my/**").authenticated()

                        //나머지 API는 일단 열어두고 추후 수정
                        .anyRequest().permitAll()
                );

        // 필터 순서
        // 예외 필터는 가장 앞에
        http.addFilterBefore(securityExceptionFilter, UsernamePasswordAuthenticationFilter.class);

        // JWT 검증 필터: UsernamePasswordAuthenticationFilter보다 먼저 실행
        http.addFilterBefore(jwtVerificationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    //비밀번호 암호화 (회원가입/비밀번호 변경 시 사용)
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
