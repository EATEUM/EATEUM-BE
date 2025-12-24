package com.eateum.eateumbe.global.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.IOException;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // /profile/** 로 들어오는 요청을 로컬 폴더(C:/upload/profile/)의 파일로 매핑
        // 예) /profile/a.jpg  ->  C:/upload/profile/a.jpg
        registry.addResourceHandler("/profile/**")
                .addResourceLocations("file:///C:/upload/profile/");
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
    }

    @Component
    public class PnaFilter implements Filter {
        @Override
        public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
                throws IOException, ServletException {
            HttpServletResponse res = (HttpServletResponse) response;
            // 브라우저에게 프라이빗 네트워크 접근이 괜찮다고 알려줌
            res.setHeader("Access-Control-Allow-Private-Network", "true");
            chain.doFilter(request, response);
        }
    }
}