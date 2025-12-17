package com.eateum.eateumbe.fridges.service;

import java.io.IOException;
import java.net.URI;
import java.util.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;


@Slf4j
@Service
@RequiredArgsConstructor
public class GeminiService {

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.api.url}")
    private String apiUrl;

    private final ObjectMapper objectMapper;

    // WebClient 오류로 RestClient 사용
    private final RestClient restClient;

    public List<String> uploadImg(MultipartFile file) {
        try {
            String base64Img = Base64.getEncoder().encodeToString(file.getBytes());
            Map<String, Object> requestBody = createRequestBody(base64Img, file.getContentType());

            //URL 조합 (공백 제거)
            String safeApiUrl = (apiUrl != null) ? apiUrl.trim() : "";
            String safeApiKey = (apiKey != null) ? apiKey.trim() : "";
            String urlString = safeApiUrl + "?key=" + safeApiKey;

            URI uri = URI.create(urlString);
            log.info("Gemini 요청 URI (RestClient): {}", uri);

            String response = restClient.post()
                    .uri(uri)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody)
                    .retrieve()
                    .body(String.class);

            return parseGeminiResponse(response);

        } catch (HttpClientErrorException e) {
            // 구체적인 에러 내용을 로그에 찍어준다.
            log.error("Gemini API 호출 에러: 상태코드={}, 내용={}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("Gemini API 호출 실패: " + e.getResponseBodyAsString(), e);
        } catch (IOException e) {
            log.error("이미지 처리 실패", e);
            throw new RuntimeException("이미지 처리 중 오류 발생", e);
        } catch (Exception e) {
            log.error("알 수 없는 오류 발생", e);
            throw new RuntimeException("시스템 오류 발생: " + e.getMessage(), e);
        }
    }

    private List<String> parseGeminiResponse(String jsonResponse) {
        List<String> resultList = new ArrayList<>();
        try {
            JsonNode root = objectMapper.readTree(jsonResponse);
            if (root.path("candidates").isEmpty()) {
                log.warn("Gemini 응답에 후보(candidates)가 없습니다.");
                return resultList;
            }
            String text = root.path("candidates").get(0)
                    .path("content")
                    .path("parts").get(0)
                    .path("text").asText();

            String[] items = text.split(",");
            for (String item : items) {
                resultList.add(item.trim());
            }
        } catch (Exception e) {
            log.error("JSON 파싱 실패. 응답값: {}", jsonResponse, e);
        }
        return resultList;
    }

    private Map<String, Object> createRequestBody(String base64Img, String mimeType) {
        Map<String, Object> textPart = new HashMap<>();
        String prompt = """
                이 냉장고 사진을 분석해서 식재료 이름만 정확하게 쉼표(,)로 구분해서 한국어로 알려줘.
                설명이나 다른 말은 하지 말고, 결과 텍스트만 출력해.

                [제약 사항]
                1. 아래 [허용된 재료 목록]에 있는 단어만 사용해야 해.
                2. 사진 속 재료가 [허용된 재료 목록]에 없다면, 가장 비슷한 재료나 상위 카테고리 이름으로 바꿔서 대답해.
                
                [매핑 및 대체 규칙]
                - (파 종류): 쪽파, 대파, 실파 -> '파'
                - (고추 종류): 청양고추, 풋고추, 홍고추 -> '고추'
                - (배추 종류): 알배기배추, 봄동 -> '배추'
                - (호박 종류): 애호박, 쥬키니호박, 늙은호박 -> '호박'
                - (버섯 종류): 느타리버섯, 표고버섯, 팽이버섯, 새송이버섯 -> 각각의 이름이 목록에 없으면 '버섯'이라고 하거나 가장 비슷한 버섯 이름 선택
                
                [특수 대체 규칙]
                - 가쓰오국수장국 -> '쯔유'
                - 어묵 육수 -> '쯔유'
                - 육수용 다시팩 -> '멸치,다시마' (두 개로 분리해서 출력)

                [허용된 재료 목록]
                쌀, 고춧가루, 간장, 된장, 소금, 설탕, 후추, 깨소금, 꿀, 참기름, 들기름, 식초, 액젓,
                감자, 고구마, 당면, 양파, 마늘, 파, 고추, 배추, 무, 상추, 깻잎, 시금치, 콩나물, 숙주나물,
                오이, 호박, 가지, 당근, 파프리카, 피망, 브로콜리, 토마토, 부추, 미나리, 생강, 양배추,
                연근, 우엉, 고사리, 도라지, 더덕, 쑥, 열무, 느타리버섯, 팽이버섯, 표고버섯, 양송이버섯,
                닭고기, 돼지고기, 소고기, 달걀, 메추리알, 우유, 콩, 고등어, 갈치, 오징어, 새우, 게,
                바지락, 홍합, 전복, 굴, 멸치, 조기, 명태, 동태, 꽁치, 삼치, 문어, 김, 미역, 다시마,
                사과, 배, 바나나, 레몬, 스팸, 김치, 치즈, 햄, 올리고당, 맛술, 미림, 치킨스톡, 멸치액젓,
                쌈장, 고추장, 카레가루, 치킨파우더, 토마토소스, 케찹, 마요네즈, 머스타드, 핫소스, 바질,
                파슬리, 월계수잎, 로즈마리, 타임, 두부, 순두부, 두유, 베이컨, 소시지, 삼겹살, 버터,
                마가린, 라면, 우동면, 국수면, 파스타면, 콩가루, 부침가루, 튀김가루, 밀가루, 식빵,
                바게트, 또띠야, 전분, 옥수수, 양상추, 로메인, 케일, 루꼴라, 샐러리, 아스파라거스, 올리브,
                파인애플, 아보카도, 라임, 참치캔, 옥수수캔, 고추참치, 베이크드빈, 황태채, 북어채, 만두,
                떡, 어묵, 굴소스, 맛살, 고추냉이, 밥, 땅콩, 올리브오일, 깨, 두반장, 애호박, 소면,
                땅콩버터, 물엿, 매실액, 가쓰오부시, 돈까스소스, 연어, 스리라차, 핫케이크믹스, 메이플시럽,
                유부, 곤약, 쯔유, 쑥갓, 페퍼론치노, 돈가스소스, 춘장, 후르츠칵테일, 라이스페이퍼, 청경채,
                생크림, 죽순채, 다시다, 초장

                예시 결과: 계란,우유,양파,파,멸치,다시마
                """;

        textPart.put("text", prompt);

        Map<String, Object> imagePart = new HashMap<>();
        Map<String, Object> inlineData = new HashMap<>();
        inlineData.put("mime_type", (mimeType != null) ? mimeType : "image/jpeg");
        inlineData.put("data", base64Img);
        imagePart.put("inline_data", inlineData);

        List<Map<String, Object>> parts = new ArrayList<>();
        parts.add(textPart);
        parts.add(imagePart);

        Map<String, Object> content = new HashMap<>();
        content.put("parts", parts);

        List<Map<String, Object>> contents = new ArrayList<>();
        contents.add(content);

        Map<String, Object> body = new HashMap<>();
        body.put("contents", contents);

        return body;
    }
}