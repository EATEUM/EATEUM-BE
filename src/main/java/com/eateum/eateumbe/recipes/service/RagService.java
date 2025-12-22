package com.eateum.eateumbe.recipes.service;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class RagService {

    private final RestClient restClient;

    @Value("${ai.server.url}")
    private String ragApiUrl;

    public List<Long> getRecommendedIds(List<String> selectedItems) {

        try {
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("selectedItems", selectedItems);

            // Fluent API 방식
            RagResponse response = restClient.post()
                    .uri(ragApiUrl)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody)
                    .retrieve() // 요청 전송 및 응답 수신 시작
                    .body(RagResponse.class); // 응답 -> 객체로 변환

            if (response != null && response.getRecipe_ids() != null) {
                return response.getRecipe_ids();
            }

        } catch (Exception e) {
            log.error("RAG 서버 통신 오류: ", e);
        }
        return List.of();
    }

    @Data
    private static class RagResponse {
        private List<Long> recipe_ids;
    }
}