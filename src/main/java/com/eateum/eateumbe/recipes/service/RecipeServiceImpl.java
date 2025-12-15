package com.eateum.eateumbe.recipes.service;

import com.eateum.eateumbe.memo.dto.response.MemoResponse;
import com.eateum.eateumbe.memo.service.MemoService;
import com.eateum.eateumbe.recipes.domain.Recipe;
import com.eateum.eateumbe.recipes.dto.request.RecipeRequest;
import com.eateum.eateumbe.recipes.dto.response.RecipeDetailResponse;
import com.eateum.eateumbe.recipes.dto.response.RecipeResponse;
import com.eateum.eateumbe.recipes.repository.RecipeMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecipeServiceImpl implements RecipeService {

    private final RecipeMapper recipeMapper;
    private final RagService ragService;
    private final MemoService memoService;

    // 💡 임시 유저 ID 획득 메서드 (실제 구현 시 Security Context에서 가져와야 함)
    private Long getCurrentUserId() {
        return 1L; // 임시 테스트용
    }

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

    @Override
    public List<RecipeResponse.Recommend> recommendPopularRecipes() {
        List<Recipe> recipes = recipeMapper.selectPopularRecipes();

        return recipes.stream()
                .map(RecipeResponse.Recommend::from)
                .collect(Collectors.toList());
    }

    @Override
    public RecipeDetailResponse getRecipeDetail(Long recipeVideoId, Boolean includeMemo) {

        Recipe recipe = recipeMapper.selectRecipeDetail(recipeVideoId);
        if (recipe == null) {
            throw new IllegalArgumentException("존재하지 않는 레시피입니다.");
        }

        // 좋아요 / 완료
        Long userId = getCurrentUserId();
        Boolean isLiked = recipeMapper.selectIsLiked(recipeVideoId, userId);
        Boolean isCompleted = recipeMapper.selectIsCompleted(recipeVideoId, userId);

        // 추천 동영상
        // includeMemo가 false(일반 상세페이지)일 때만 추천 영상을 가져옴
        List<Recipe> relatedVideos = !includeMemo ?
                recipeMapper.selectRelatedVideos(recipeVideoId, recipe.getCategoryId()) :
                Collections.emptyList();

        // 메모
        // includeMemo가 false(일반 상세페이지)면 비어 있는 리스트[] 전달
        // true(마이페이지)일 경우만 메모 가져옴 , 그러나 만약 아무것도 작성되지 않았을 경우 빈 리스트 [] 전달하도록 memoService 에 조건문 추가함
        List<MemoResponse> memos = includeMemo ?
                memoService.getMemosByRecipe(recipeVideoId, userId) :
                Collections.emptyList();

        return RecipeDetailResponse.from(recipe, isLiked, isCompleted, relatedVideos, memos);
    }
}