package com.eateum.eateumbe.recipes.controller;

import com.eateum.eateumbe.global.common.ApiResponse;
import com.eateum.eateumbe.global.common.BaseController;
import com.eateum.eateumbe.global.common.PageResponse;
import com.eateum.eateumbe.recipes.dto.request.RecipeRequest;
import com.eateum.eateumbe.recipes.dto.response.RecipeDashboardResponse;
import com.eateum.eateumbe.recipes.dto.response.RecipeDetailResponse;
import com.eateum.eateumbe.recipes.dto.response.RecipeResponse;
import com.eateum.eateumbe.recipes.service.RecipeService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/recipes")
@RequiredArgsConstructor
public class RecipeController extends BaseController {

    private final RecipeService recipeService;

    @Operation(summary = "AI 추천 레시피")
    @PostMapping("/recommend/ai")
    public ApiResponse<List<RecipeResponse.Recommend>> recommendAi(@AuthenticationPrincipal String userId,
            @RequestBody RecipeRequest.Recommend request) {

        String safeUserId = resolveUserId(userId);

        List<RecipeResponse.Recommend> results = recipeService.recommendAiRecipes(request, safeUserId);

        return ApiResponse.success(results);
    }

    @Operation(summary = "15분 컷 레시피")
    @GetMapping("/recommend/speed")
    public ApiResponse<List<RecipeResponse.Recommend>> recommendSpeed() {
        List<RecipeResponse.Recommend> results = recipeService.recommendSpeedRecipes();
        return ApiResponse.success(results);
    }

    @Operation(summary = "지금 뜨고 있는 레시피")
    @GetMapping("/recommend/popular")
    public ApiResponse<List<RecipeResponse.Recommend>> recommendPopular() {
        List<RecipeResponse.Recommend> results = recipeService.recommendPopularRecipes();
        return ApiResponse.success(results);
    }

    @Operation(summary = "레시피 상세 조회")
    @GetMapping("/{recipe_video_id}/detail")
    public ApiResponse<RecipeDetailResponse> getRecipeDetail(
            @AuthenticationPrincipal String userId,
            @PathVariable("recipe_video_id") Long recipeVideoId,
            @RequestParam(value = "include_memo", defaultValue = "false") boolean includeMemo) {
        String safeUserId = resolveUserId(userId);

        RecipeDetailResponse response = recipeService.getRecipeDetail(
                safeUserId,
                recipeVideoId,
                includeMemo

        );
        return ApiResponse.success(response);
    }

    @Operation(summary = "마이 페이지")
    @GetMapping("/my")
    public ApiResponse<PageResponse<RecipeResponse.Status>> getStatusRecipes(
            @AuthenticationPrincipal String userId,
            @RequestParam("status") String status,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "9") int size) {
        String safeUserId = requireAuth(userId);
        PageResponse<RecipeResponse.Status> result = recipeService.getStatusRecipes(safeUserId, status, page, size);
        return ApiResponse.success(result);
    }

    @Operation(summary = "마이페이지 대시보드")
    @GetMapping("my/dashboard")
    public ApiResponse<RecipeDashboardResponse> getDashboardRecipes(@AuthenticationPrincipal String userId) {
        String safeUserId = requireAuth(userId);
        RecipeDashboardResponse response = recipeService.getRecipeDashboard(safeUserId);
        return ApiResponse.success(response);
    }

    @Operation(summary = "좋아요 버튼")
    @PostMapping("{recipe_video_id}/like")
    public ApiResponse<Void> buttonLike(
            @AuthenticationPrincipal String userId,
            @PathVariable("recipe_video_id") Long recipeVideoId

    ) {
        String safeUserId = requireAuth(userId);

        recipeService.buttonLike(safeUserId, recipeVideoId);
        return ApiResponse.success(null);
    }

    @Operation(summary = "완성 버튼")
    @PostMapping("{recipe_video_id}/complete")
    public ApiResponse<Void> buttonComplete(
            @AuthenticationPrincipal String userId,
            @PathVariable("recipe_video_id") Long recipeVideoId

    ) {
        String safeUserId = requireAuth(userId);

        recipeService.buttonComplete(safeUserId, recipeVideoId);
        return ApiResponse.success(null);
    }
}