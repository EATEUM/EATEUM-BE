package com.eateum.eateumbe.recipes.controller;

import com.eateum.eateumbe.global.common.ApiResponse;
import com.eateum.eateumbe.recipes.dto.request.RecipeRequest;
import com.eateum.eateumbe.recipes.dto.response.RecipeDetailResponse;
import com.eateum.eateumbe.recipes.dto.response.RecipeResponse;
import com.eateum.eateumbe.recipes.service.RecipeService;
import lombok.RequiredArgsConstructor;
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

}