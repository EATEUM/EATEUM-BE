package com.eateum.eateumbe.recipes.service;

import com.eateum.eateumbe.global.common.PageResponse;
import com.eateum.eateumbe.recipes.dto.request.RecipeRequest;
import com.eateum.eateumbe.recipes.dto.response.RecipeDashboardResponse;
import com.eateum.eateumbe.recipes.dto.response.RecipeDetailResponse;
import com.eateum.eateumbe.recipes.dto.response.RecipeResponse;

import java.util.List;

public interface RecipeService {

    //AI 추천 레시피
    List<RecipeResponse.Recommend> recommendAiRecipes(RecipeRequest.Recommend request, String userId);

    // 15분컷 레시피
    List<RecipeResponse.Recommend> recommendSpeedRecipes();

    // 지금 뜨고 있는 레시피
    List<RecipeResponse.Recommend> recommendPopularRecipes();

    // 레시피 상세 조회
    RecipeDetailResponse getRecipeDetail(String userId, Long recipeVideoId, Boolean includeMemo);

    //  완성 or 좋아요 에 따른 조회(마이페이지)
    PageResponse<RecipeResponse.Status> getStatusRecipes(String userId, String status, int page, int size);

    // 레시피 대시보드
    RecipeDashboardResponse getRecipeDashboard(String userId);
 }