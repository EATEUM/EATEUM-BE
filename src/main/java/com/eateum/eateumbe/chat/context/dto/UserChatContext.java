package com.eateum.eateumbe.chat.context.dto;

import com.eateum.eateumbe.chat.dto.response.RecipeStep;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * 회원 챗봇 응답을 위한 사용자 컨텍스트 데이터
 * - Prompt에 그대로 주입되는 데이터 묶음
 */
@Getter
@Builder
public class UserChatContext {

    private List<RecipeBrief> recentCompleted;

    private List<RecipeBrief> recentLiked;

    private List<CategoryStat> topCategories;

    private List<RecipeMemoBrief> recentMemos;

    private List<RecipeStep> recipeSteps;

}
