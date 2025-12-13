package com.eateum.eateumbe.recipes.controller;

import com.eateum.eateumbe.global.common.ApiResponse;
import com.eateum.eateumbe.recipes.dto.request.RecipeRequest;
import com.eateum.eateumbe.recipes.dto.response.RecipeResponse;
import com.eateum.eateumbe.recipes.service.RecipeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/recipes")
@RequiredArgsConstructor
public class RecipeController {

    private final RecipeService recipeService;

    @PostMapping("/recommend/ai")
    public ApiResponse<List<RecipeResponse>> recommend(@RequestBody RecipeRequest request) {

        List<RecipeResponse> results = recipeService.recommendRecipes(request);

        return ApiResponse.success(results);
    }
}