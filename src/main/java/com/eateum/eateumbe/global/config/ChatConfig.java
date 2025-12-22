package com.eateum.eateumbe.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class ChatConfig {

    //AI 서버로 나가는 모든 요청에 대한 공통 RestClient 설정
    @Bean
    public RestClient.Builder restClientBuilder() {
        return RestClient.builder()
                .requestInterceptor(((request, body, execution) -> {
                    request.getHeaders().set("Content-Type", "application/json");
                    return execution.execute(request, body);
                }));
    }


}
