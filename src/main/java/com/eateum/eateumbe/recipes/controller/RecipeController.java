package com.eateum.eateumbe.recipes.controller;

import com.eateum.eateumbe.global.common.ApiResponse;
import com.eateum.eateumbe.global.common.BaseController;
import com.eateum.eateumbe.global.common.PageResponse;
import com.eateum.eateumbe.global.error.ApiException;
import com.eateum.eateumbe.recipes.dto.request.RecipeRequest;
import com.eateum.eateumbe.recipes.dto.response.RecipeDashboardResponse;
import com.eateum.eateumbe.recipes.dto.response.RecipeDetailResponse;
import com.eateum.eateumbe.recipes.dto.response.RecipeResponse;
import com.eateum.eateumbe.recipes.service.RecipeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/recipes")
@RequiredArgsConstructor
public class RecipeController extends BaseController {

    private final RecipeService recipeService;

    @PostMapping("/recommend/ai")
    public ApiResponse<List<RecipeResponse.Recommend>> recommendAi(@AuthenticationPrincipal String userId, @RequestBody RecipeRequest.Recommend request) {

        String safeUserId = resolveUserId(userId);

        List<RecipeResponse.Recommend> results = recipeService.recommendAiRecipes(request, safeUserId);

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
        @AuthenticationPrincipal String userId,
        @PathVariable("recipe_video_id") Long recipeVideoId,
        @RequestParam(value = "include_memo", defaultValue = "false") boolean includeMemo
    ){
        String safeUserId = resolveUserId(userId);

        RecipeDetailResponse response = recipeService.getRecipeDetail(
            safeUserId,
            recipeVideoId,
            includeMemo

        );
        return ApiResponse.success(response);
    }

    @GetMapping("/my")
    public ApiResponse<PageResponse<RecipeResponse.Status>> getStatusRecipes(
            @AuthenticationPrincipal String userId,
            @RequestParam("status") String status,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "9") int size
    ) {

        PageResponse<RecipeResponse.Status> result =  recipeService.getStatusRecipes(userId, status, page, size);
        return ApiResponse.success(result);
    }

    @GetMapping("my/dashboard")
    public ApiResponse<RecipeDashboardResponse> getDashboardRecipes(@AuthenticationPrincipal String userId) {
        RecipeDashboardResponse response = recipeService.getRecipeDashboard(userId);
        return ApiResponse.success(response);
    }

    @PostMapping("{recipe_video_id}/like")
    public ApiResponse<Void> buttonLike(
            @AuthenticationPrincipal String userId,
            @PathVariable("recipe_video_id") Long recipeVideoId

    ){
        String safeUserId = resolveUserId(userId);

        if ("guest".equals(safeUserId)) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "로그인이 필요한 기능입니다.");
        }

        recipeService.buttonLike(safeUserId, recipeVideoId);
        return ApiResponse.success(null);
    }

    @PostMapping("{recipe_video_id}/complete")
    public ApiResponse<Void> buttonComplete(
            @AuthenticationPrincipal String userId,
            @PathVariable("recipe_video_id") Long recipeVideoId

    ){
        String safeUserId = resolveUserId(userId);

        if ("guest".equals(safeUserId)) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "로그인이 필요한 기능입니다.");
        }

        recipeService.buttonComplete(safeUserId, recipeVideoId);
        return ApiResponse.success(null);
    }
}