package com.eateum.eateumbe.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // /profile/** 로 들어오는 요청을 로컬 폴더(C:/upload/profile/)의 파일로 매핑
        // 예) /profile/a.jpg  ->  C:/upload/profile/a.jpg
        registry.addResourceHandler("/profile/**")
                .addResourceLocations("file:///C:/upload/profile/");
    }
}
