package com.eateum.eateumbe.recipes.controller;

import com.eateum.eateumbe.global.common.ApiResponse;
import com.eateum.eateumbe.global.common.PageResponse;
import com.eateum.eateumbe.recipes.dto.request.RecipeRequest;
import com.eateum.eateumbe.recipes.dto.response.RecipeDashboardResponse;
import com.eateum.eateumbe.recipes.dto.response.RecipeDetailResponse;
import com.eateum.eateumbe.recipes.dto.response.RecipeResponse;
import com.eateum.eateumbe.recipes.service.RecipeService;
import lombok.RequiredArgsConstructor;
import org.apache.ibatis.annotations.Param;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/recipes")
@RequiredArgsConstructor
public class RecipeController {

    private final RecipeService recipeService;

    @PostMapping("/recommend/ai")
    public ApiResponse<List<RecipeResponse.Recommend>> recommendAi(@RequestBody RecipeRequest.Recommend request) {

        List<RecipeResponse.Recommend> results = recipeService.recommendAiRecipes(request);

        return ApiResponse.success(results);
    }

    @GetMapping("/recommend/speed")
    public ApiResponse<List<RecipeResponse.Recommend>> recommendSpeed() {
        List<RecipeResponse.Recommend> results = recipeService.recommendSpeedRecipes();
        return ApiResponse.success(results);
    }


    @GetMapping("/recommend/popular")
    public ApiResponse<List<RecipeResponse.Recommend>> recommendPopular() {
        List<RecipeResponse.Recommend> results = recipeService.recommendPopularRecipes();
        return ApiResponse.success(results);
    }

    @GetMapping("/{recipe_video_id}/detail")
    public ApiResponse<RecipeDetailResponse> getRecipeDetail(
        @PathVariable("recipe_video_id") Long recipeVideoId,
        @RequestParam(value = "include_memo", defaultValue = "false") boolean includeMemo
    ){
        RecipeDetailResponse response = recipeService.getRecipeDetail(
            recipeVideoId,
            includeMemo
        );
        return ApiResponse.success(response);
    }

    // 임시 유저 ID 획득 메서드 (실제 구현 시 Security Context에서 가져와야 함)
    private Long getCurrentUserId() {
        return 1L; // 임시 테스트용
    }

    @GetMapping("/my")
    public ApiResponse<PageResponse<RecipeResponse.Status>> getStatusRecipes(
            @RequestParam("status") String status,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "9") int size
    ) {
        Long userId = getCurrentUserId();

        PageResponse<RecipeResponse.Status> result =  recipeService.getStatusRecipes(userId, status, page, size);
        return ApiResponse.success(result);
    }

    @GetMapping("my/dashboard")
    public ApiResponse<RecipeDashboardResponse> getDashboardRecipes() {
        Long userId = getCurrentUserId();
        RecipeDashboardResponse response = recipeService.getRecipeDashboard(userId);
        return ApiResponse.success(response);
    }
}