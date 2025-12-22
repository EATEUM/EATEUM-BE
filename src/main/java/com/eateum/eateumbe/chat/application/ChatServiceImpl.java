package com.eateum.eateumbe.chat.application;

import com.eateum.eateumbe.chat.context.dto.UserChatContext;
import com.eateum.eateumbe.chat.memory.ChatMemory;
import com.eateum.eateumbe.chat.memory.ChatMemoryRouter;
import com.eateum.eateumbe.chat.memory.ChatMessage;
import com.eateum.eateumbe.chat.memory.RedisChatMemory;
import com.eateum.eateumbe.chat.context.service.ChatContextFormatter;
import com.eateum.eateumbe.chat.context.service.UserChatContextService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * 챗봇 핵심 서비스 구현체
 *
 * 역할:
 * - 회원 / 비회원 요청 분기
 * - 대화 메모리 조회 및 저장
 * - 사용자 컨텍스트를 Propt에 결합
 * - AI 호출
 */
@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements  ChatService {
    
    private final ChatModel chatModel; //AI 호출 엔진
    private final ChatMemoryRouter memoryRouter; //대화 메모리
    private final UserChatContextService userChatContextService; //회원 컨텍스트
    private final RedisChatMemory redisChatMemory;

    //비회원
    public String chatForGuest(String sessionId, String message) {

        ChatMemory memory = memoryRouter.getForGuest();
        String key = sessionId;

        List<ChatMessage> history = memory.load(key);

        Prompt prompt = buildPromptForGuest(history, message);
        String answer = callAi(prompt);

        memory.save(key, new ChatMessage(ChatMessage.Role.USER, message));
        memory.save(key, new ChatMessage(ChatMessage.Role.ASSISTANT, answer));

        return answer;
    }

    //회원
    @Override
    public String chatForMember(String userId, String message) {

        ChatMemory memory = memoryRouter.getForMember();

        //이전 대화 기록 로드
        List<ChatMessage> history = memory.load(userId);

        boolean isFirstAfterExpire = history.isEmpty();

        if(isFirstAfterExpire) {
            history.add(
                    new ChatMessage(ChatMessage.Role.SYSTEM,
                            """
                                    이전 대화는 요약만 남아있습니다.
                                    이전에 이야기했던 맥락을 참고해 대화를 이어가 주세요. 
                                    """)
            );
        }

        //요약 로드 (없으면 null)
        String summary = redisChatMemory.loadSummary(userId);

        //DB에서 회원 컨텍스트 로드
        UserChatContext context = userChatContextService.load(userId);
        String contextText = ChatContextFormatter.format(context);

        //이전 대화 + 새 질문으로 Prompt 생성
        Prompt prompt = buildPromptForMember(history, summary, contextText, message); //최근대화+ 요약 + 개인정보 + 새 질문

//        logPrompt("MEMBER", userId, prompt); //프롬프트 디버깅용

        //AI 호출
        String answer = callAi(prompt);

        //대화 저장
        memory.save(userId, new ChatMessage(ChatMessage.Role.USER, message));
        memory.save(userId, new ChatMessage(ChatMessage.Role.ASSISTANT, answer));

        return answer;
    }

    //Prompt 빌더
    private Prompt buildPromptForGuest(List<ChatMessage> history, String message) {

        String conversationHistory = buildConversationHistory(history);

        PromptTemplate template = new PromptTemplate("""
            너는 요리 서비스 EATEUM의 챗봇이야.
            처음 방문한 사용자에게 서비스를 설명하는 역할이야.
            
            서비스 규칙:
            - 비회원도 '내 냉장고 레시피 추천받기' 기능에서는
              기본 재료(돼지고기, 스팸, 달걀, 김치, 라면, 양파, 파)로
              레시피 추천을 받을 수 있다.
            - 하지만 이 챗봇에서는 레시피를 직접 추천하지 않는다.
            - 챗봇에서는 기능 설명, 사용 방법, 요리 관련 일반 정보만 제공한다.
            - 더 많은 재료 기반 추천이나 개인화 기능은 회원 전용이다.
            
            말투:
            - 반말 X
            - 과도한 이모지 X
            - 친근하지만 정보 위주
            
            답변 가이드:
            - 레시피 추천 요청이 오면
             → 해당 기능은 '내 냉장고 레시피 추천받기'에서 이용 가능하다고 안내한다.
            - 기본 재료를 벗어난 추천 요청이 오면
             → 회원 가입 시 가능한 기능임을 설명한다.
            - 답변은 친절하고 간결하게 한다.
            
            이전 대화: {history}
            
            사용자 질문: {message}
        """);

        return template.create(Map.of(
                "history", conversationHistory,
                "message", message
        ));
    }

    private Prompt buildPromptForMember(List<ChatMessage> history, String summary, String context, String message) {

        String conversationHistory = buildConversationHistory(history);
        String safeSummary = (summary == null) ? "" : summary;

        PromptTemplate template = new PromptTemplate("""
            너는 요리 서비스 EATEUM의 개인 맞춤 챗봇이야.
            이전 대화가 일부 생략될 수 있지만,
            사용자의 요리 기록과 관심사를 이해한 상태로 대답해.
            
            역할:
            - 사용자의 취향을 이해한 것처럼 자연스럽게 대답한다.
            - 요리와 관련된 질문에 설명, 팁, 조언을 제공한다.
            - 이해하기 쉬운 수평적인 말투를 유지한다.
            
            영상 및 레시피 정보 활용 규칙:
            - 사용자가 완료하거나 관심 표시한 레시피에 연결된 유튜브 영상은
              '요리 맥락을 이해하기 위한 참고 정보'로만 활용한다.
            - 영상의 특정 장면, 시간, 정확한 발언을 인용하거나
              실제로 영상을 분석한 것처럼 말하지 않는다.
            - 일반적인 요리 상식과 경험을 바탕으로 조언한다.
            
            레시피 단계 정보 활용 가이드 (recipe_steps):
            - recipe_steps는 '조리 흐름을 이해하기 위한 참고 자료'로만 사용한다.
            - 레시피를 단계별로 그대로 재현하거나 나열하지 않는다.
            - 대신 다음과 같은 방식으로 활용한다:
              • 특정 재료가 등장하는 단계의 목적 설명
              • 그 단계에서 자주 발생하는 실수
              • 대체 가능한 방법이나 선택지
              • 왜 그 순서가 중요한지에 대한 이유
            - 예시:
              ❌ "1단계에서 파를 썰고, 2단계에서 볶는다"
              ✅ "파를 이 단계에서 넣는 이유는 향을 먼저 내기 위함이에요"
        
            - 사용자가 특정 재료나 과정에 대해 물으면,
              해당 재료가 등장하는 단계의 '의도와 요령' 위주로 답변한다.
            - recipe_steps는 답변의 '근거'로만 사용하고, 답변에 그대로 노출하지 않는다.
                
            정보 활용 우선순위:
            1. 사용자의 질문과 직접 관련된 recipe_steps
            2. 관련된 요리 메모 (실수, 팁)
            3. 최근 완성 / 관심 요리 맥락
            4. 일반적인 요리 상식
        
            - 모든 정보를 다 쓰려고 하지 말고,
              질문에 가장 도움이 되는 정보만 선택해서 사용한다.
                
            허용되는 질문 예시:
            - 이 요리에서 재료를 다른 재료로 대체해도 될지
            - 이 요리를 더 맛있게 만드는 팁이나 주의할 점
            - 특정 재료(파, 마늘 등)을 어떻게 손질하면 좋을지
            - 요리 선택 시 고려하면 좋은 기준
            
            출력 가이드:
            - 사용자가 "목록", "몇 개", "알려줘"와 같이 나열을 요청하면
              항목별로 정리해서 답변한다.
            - 요리 목록을 말할 때는 다음 순서를 따른다:
              1. 요리 이름
              2. 관련 메모가 있다면 함께 언급
            - 목록은 쉼표가 아닌 줄바꿈으로 구분한다.
            
            중요 규칙:
            - 레시피 "추천"은 하지 않는다.
            - 실제 레시피 선택은 '내 냉장고 레시피 추천받기' 기능에서만 가능하다.
            - 여기서는 요리 정보, 조리 팁, 선택 기준, 응용방법만 제공한다.
            - 사용자가 하지 않은 요리 경험을 지어내지 않는다.
            - 데이터에 없는 정보를 사실처럼 말하지 않는다.
            - 레시피를 단계별로 상세히 재현하지 않는다.
            
            답변 스타일:
            - 너무 길지 않게 (3~6줄)
            - 친절하지만 전문가 느낌
            - 필요하면 자연스러운 질문으로 대화를 이어간다.
            
            답변 시작 가이드:
            - 가능하다면 다음 표현 중 하나로 답변을 시작한다:
              • "최근에 하신 요리를 보면,"
              • "아까 말씀하신 요리 기준으로 보면,"
              • "완성하신 레시피 흐름을 보면,"
            - 실제로 없는 기억은 만들어내지 않는다.
            
            이전 대화 요약: {summary}
            
            사용자 정보 요약: {context}
            
            최근 대화: {history}
            
            사용자 질문: {message}
            
            답변 시 유의:
            - 사용자의 과거 요리/좋아요 기록이 있다면 자연스럽게 언급 가능
            - 없는 정보는 추측하지 않는다.
        """);

        return template.create(Map.of(
                "summary", safeSummary,
                "context", context,
                "history", conversationHistory,
                "message", message
        ));
    }

    private String callAi(Prompt prompt) {
        return chatModel.call(prompt)
                .getResult()
                .getOutput()
                .getText();
    }

    private String buildConversationHistory(List<ChatMessage> history) {
        if(history == null || history.isEmpty()) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        for (ChatMessage msg : history) {
            sb.append(msg.getRole().name())
                    .append(": ")
                    .append(msg.getContent())
                    .append("\n");
        }
        return sb.toString();
    }
    
    //프롬프트 디버깅용
//    private void logPrompt(String type, String key, Prompt prompt) {
//        String text = prompt.getInstructions().toString();
//        int charLength = text.length();
//        int estimatedTokens = charLength / 4; //매우 보편적인 추정치
//
//        System.out.println("\n==============================");
//        System.out.println("🧠 CHATBOT PROMPT DEBUG");
//        System.out.println("TYPE : " + type);
//        System.out.println("KEY  : " + key);
//        System.out.println("CHARS : " + charLength);
//        System.out.println("TOKENS : ~" + estimatedTokens);
//        System.out.println("------------------------------");
//
//        System.out.println(text);
//
//        System.out.println("==============================\n");
//    }

    @Override
    public List<ChatMessage> getHistoryForGuest(String sessionId) {
        ChatMemory memory = memoryRouter.getForGuest();
        return memory.load(sessionId);
    }

    @Override
    public List<ChatMessage> getHistoryForMember(String userId) {
        ChatMemory memory = memoryRouter.getForMember();
        return memory.load(userId);
    }
}
