package com.eateum.eateumbe.chat.context.service;

import com.eateum.eateumbe.chat.context.dto.CategoryStat;
import com.eateum.eateumbe.chat.context.dto.RecipeBrief;
import com.eateum.eateumbe.chat.context.dto.RecipeMemoBrief;
import com.eateum.eateumbe.chat.context.dto.UserChatContext;
import com.eateum.eateumbe.chat.dto.response.RecipeStep;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 데이터를 요약하는 Formatter
 * ------------------------------------------------------
 * 최근에 완성한 요리: 김치찌개(한식), 파스타(양식), 제육볶음(한식)
 * 최근에 관심을 가진 요리: 마파두부, 카레
 * 요리 취향 카테고리: 한식, 양식
 * 요리 메모: "간이 조금 셌음", "불을 약하게 유지"
 */
public class ChatContextFormatter {
    
    //회원 컨텍스트를 Prompt용 문자열로 반환
    public static String format(UserChatContext userChatContext) {
        StringBuilder sb = new StringBuilder();

        appendRecentCompleted(sb, userChatContext.getRecentCompleted());
        appendRecentLiked(sb, userChatContext.getRecentLiked());
        appendTopCategories(sb, userChatContext.getTopCategories());
        appendRecentMemos(sb, userChatContext.getRecentMemos());
        appendRecipeSteps(sb, userChatContext.getRecipeSteps());

        return sb.toString().trim();
    }

    private static void appendRecentCompleted(StringBuilder sb, List<RecipeBrief> list) {
        if (list == null || list.isEmpty()) return; //비어있으면 넣지 않음 > prompt 짧게 유지

        sb.append("최근에 완성한 요리 : ");
        sb.append(
                list.stream()
                        .map(recipe -> recipe.getVideoTitle() + "(" + recipe.getCategoryName() + ")")
                        .collect(Collectors.joining(", "))
        );
        sb.append("\n");
    }

    private static void appendRecentLiked(StringBuilder sb, List<RecipeBrief> list) {
        if (list == null || list.isEmpty()) return;

        sb.append("최근에 관심을 가진 요리 : ");
        sb.append(
                list.stream()
                        .map(RecipeBrief::getVideoTitle)
                        .collect(Collectors.joining(", "))
        );
        sb.append("\n");
    }

    private static void appendTopCategories(StringBuilder sb, List<CategoryStat> list) {
        if (list == null || list.isEmpty()) return;

        sb.append("요리 취향 카테고리 : ");
        sb.append(
                list.stream()
                        .map(CategoryStat::getCategoryName)
                        .collect(Collectors.joining(", "))
        );
        sb.append("\n");
    }

    private static void appendRecentMemos(StringBuilder sb, List<RecipeMemoBrief> list) {
        if (list == null || list.isEmpty()) return;

        sb.append("요리 메모 : ");
        sb.append(
                list.stream()
                        .map(memo -> "\"" + memo.getContent() + "\"")
                        .collect(Collectors.joining(", "))
        );
        sb.append("\n");
    }

    private static void appendRecipeSteps(
            StringBuilder sb,
            List<RecipeStep> recipeSteps
    ) {
        if (recipeSteps == null || recipeSteps.isEmpty()) return;

        sb.append("요리 단계 정보:\n");

        for (RecipeStep step : recipeSteps) {
            sb.append("- [")
                    .append(step.getRecipeVideoId())
                    .append(" / ")
                    .append(step.getStepNumber())
                    .append("단계] ");

            if (step.getStepTitle() != null) {
                sb.append(step.getStepTitle()).append(" : ");
            }

            sb.append(step.getContent()).append("\n");
        }
    }

    
}
