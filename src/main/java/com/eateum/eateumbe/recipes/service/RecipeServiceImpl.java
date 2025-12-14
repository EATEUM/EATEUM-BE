package com.eateum.eateumbe.recipes.service;

import com.eateum.eateumbe.recipes.domain.Recipe;
import com.eateum.eateumbe.recipes.dto.request.RecipeRequest;
import com.eateum.eateumbe.recipes.dto.response.RecipeResponse;
import com.eateum.eateumbe.recipes.repository.RecipeMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecipeServiceImpl implements RecipeService {

    private final RecipeMapper recipeMapper;
    private final RagService ragService;

    @Override
    public List<RecipeResponse.Recommend> recommendAiRecipes(RecipeRequest.Recommend request) {

        if (request == null || request.getSelectedItems() == null) {
            return List.of();
        }

        List<String> items = request.getSelectedItems();

        if (items.isEmpty()) {
            return List.of();
        }

        // RAG 서버와 연동
        List<Long> recommendedIds = ragService.getRecommendedIds(items);

        if (recommendedIds.isEmpty()) {
            return List.of();
        }

        List<Recipe> recipes = recipeMapper.selectRecipesByIds(recommendedIds);

        // Domain(Entity) -> DTO 변환
        return recipes.stream()
                .map(RecipeResponse.Recommend::from)
                .collect(Collectors.toList());
    }

    @Override
    public List<RecipeResponse.Recommend> recommendSpeedRecipes() {
        List<Recipe> recipes = recipeMapper.selectSpeedRecipes();

        return recipes.stream()
                .map(RecipeResponse.Recommend::from)
                .collect(Collectors.toList());
    }


}