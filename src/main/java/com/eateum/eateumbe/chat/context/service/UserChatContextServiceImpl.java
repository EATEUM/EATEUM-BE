package com.eateum.eateumbe.chat.context.service;

import com.eateum.eateumbe.chat.context.dto.RecipeBrief;
import com.eateum.eateumbe.chat.context.dto.UserChatContext;
import com.eateum.eateumbe.chat.dto.response.RecipeStep;
import com.eateum.eateumbe.chat.repository.ChatbotMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Stream;

/**
 * UserChatContextService 구현체
 * - 여러 테이블 조회 결과를 조합하여 UserChatContext 객체를 생성
 */
@Service
@RequiredArgsConstructor
public class UserChatContextServiceImpl implements UserChatContextService {

    private final ChatbotMapper chatbotMapper;

    @Override
    public UserChatContext load(String userId) {

        List<RecipeBrief> completed = chatbotMapper.selectRecentCompletedRecipes(userId);
        List<RecipeBrief> liked = chatbotMapper.selectRecentLikedRecipes(userId);

        //레시피 ID 수집 (중복제거)
        List<Long> recipeVideoIds = Stream.concat(completed.stream(), liked.stream())
                .map(RecipeBrief::getRecipeVideoId)
                .distinct()
                .toList();

        List<RecipeStep> steps = recipeVideoIds.isEmpty() ? List.of() : chatbotMapper.selectStepsByRecipeIds(recipeVideoIds);

        return UserChatContext.builder()
                .recentCompleted(completed)
                .recentLiked(liked)
                .topCategories(chatbotMapper.selectTopCategories(userId))
                .recentMemos(chatbotMapper.selectRecentMemos(userId))
                .recipeSteps(steps)
                .build();
    }
}
