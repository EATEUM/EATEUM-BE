package com.eateum.eateumbe.recipes.service;

import com.eateum.eateumbe.recipes.dto.request.RecipeRequest;
import com.eateum.eateumbe.recipes.dto.response.RecipeResponse;
import java.util.List;

public interface RecipeService {

    List<RecipeResponse.Recommend> recommendRecipes(RecipeRequest.Recommend request);
}