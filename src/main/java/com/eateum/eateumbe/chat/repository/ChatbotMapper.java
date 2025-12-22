package com.eateum.eateumbe.chat.repository;

import com.eateum.eateumbe.chat.context.dto.CategoryStat;
import com.eateum.eateumbe.chat.context.dto.RecipeBrief;
import com.eateum.eateumbe.chat.context.dto.RecipeMemoBrief;
import com.eateum.eateumbe.chat.dto.response.RecipeStep;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 챗봇 전용 Mapper
 */
@Mapper
public interface ChatbotMapper {
    
    //최근 완성 레시피
    List<RecipeBrief> selectRecentCompletedRecipes(@Param("userId") String userId);

    //최근 좋아요 레시피
    List<RecipeBrief> selectRecentLikedRecipes(@Param("userId") String userId);

    //선호 카테고리
    List<CategoryStat> selectTopCategories(@Param("userId") String userId);

    //최근 메모
    List<RecipeMemoBrief> selectRecentMemos(@Param("userId") String userId);

    //레시피 단계
    List<RecipeStep> selectStepsByRecipeIds(@Param("recipeVideoIds") List<Long> recipeVideoIds);

}
