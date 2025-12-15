package com.eateum.eateumbe.recipes.service;

import com.eateum.eateumbe.recipes.domain.Recipe;
import com.eateum.eateumbe.recipes.dto.request.RecipeRequest;
import com.eateum.eateumbe.recipes.dto.response.RecipeDetailResponse;
import com.eateum.eateumbe.recipes.dto.response.RecipeResponse;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface RecipeService {

    //AI 추천 레시피
    List<RecipeResponse.Recommend> recommendAiRecipes(RecipeRequest.Recommend request);

    // 15분컷 레시피
    List<RecipeResponse.Recommend> recommendSpeedRecipes();

    // 지금 뜨고 있는 레시피
    List<RecipeResponse.Recommend> recommendPopularRecipes();

    // 레시피 상세 조회
    RecipeDetailResponse getRecipeDetail(Long recipeVideoId, boolean includeMemo);
}