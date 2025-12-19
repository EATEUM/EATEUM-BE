package com.eateum.eateumbe.recipes.service;

import com.eateum.eateumbe.fridges.repository.FridgeMapper;
import com.eateum.eateumbe.fridges.service.FridgeService;
import com.eateum.eateumbe.global.common.PageResponse;
import com.eateum.eateumbe.global.constant.RecipeCategory;
import com.eateum.eateumbe.memo.dto.response.MemoResponse;
import com.eateum.eateumbe.memo.service.MemoService;
import com.eateum.eateumbe.recipes.domain.Recipe;
import com.eateum.eateumbe.recipes.dto.request.RecipeRequest;
import com.eateum.eateumbe.recipes.dto.response.RecipeDashboardResponse;
import com.eateum.eateumbe.recipes.dto.response.RecipeDetailResponse;
import com.eateum.eateumbe.recipes.dto.response.RecipeResponse;
import com.eateum.eateumbe.recipes.repository.RecipeMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecipeServiceImpl implements RecipeService {

    private final RecipeMapper recipeMapper;
    private final FridgeMapper fridgeMapper;
    private final FridgeService fridgeService;
    private final RagService ragService;
    private final MemoService memoService;


    @Override
    public List<RecipeResponse.Recommend> recommendAiRecipes(RecipeRequest.Recommend request, String userId) {

        List<String> items;

        // 1) 회원이 냉장고에서 재료 선택한 경우
        if (request.getSelectedItems() != null && !request.getSelectedItems().isEmpty()) {
            items = request.getSelectedItems();
        }

        // 2) 재료 선택이 없는 경우 (비회원/ 첫 가입자)

        else {
            if(!"guest".equalsIgnoreCase(userId)){
                items = fridgeMapper.selectItemNamesByUserId(userId);

                if(items == null || items.isEmpty()){
                    items = fridgeService.getGuestItemNames();
                }
            }
            else{
                items = fridgeService.getGuestItemNames();
            }
        }

        // RAG 서버와 연동
        List<Long> recommendedIds = ragService.getRecommendedIds(items);

        if (recommendedIds.isEmpty()) {
            return List.of();
        }

        List<Recipe> recipes = recipeMapper.selectRecipesByIds(recommendedIds);

        return recipeMapper.selectRecipesByIds(recommendedIds)
                .stream()
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
    public RecipeDetailResponse getRecipeDetail(String userId, Long recipeVideoId, Boolean includeMemo) {

        Recipe recipe = recipeMapper.selectRecipeDetail(recipeVideoId, userId);
        if (recipe == null) {
            throw new IllegalArgumentException("존재하지 않는 레시피입니다.");
        }

        Boolean isLiked = false;
        Boolean isCompleted = false;

        // 비회원(guest)이면 좋아요, 완성, 메모 조회 생략
        if (!"guest".equalsIgnoreCase(userId)) {
            isLiked = recipeMapper.selectIsLiked(recipeVideoId, userId);
            isCompleted = recipeMapper.selectIsCompleted(recipeVideoId, userId);
        }

        // 추천 동영상
        // includeMemo가 false(일반 상세페이지)일 때만 추천 영상을 가져옴
        List<Recipe> relatedVideos = !includeMemo ?
                recipeMapper.selectRelatedVideos(recipeVideoId, recipe.getCategoryId()) :
                Collections.emptyList();

        // 메모
        // includeMemo가 false(일반 상세페이지)면 비어 있는 리스트[] 전달
        // true(마이페이지)일 경우만 메모 가져옴 , 그러나 만약 아무것도 작성되지 않았을 경우 빈 리스트 [] 전달하도록 memoService 에 조건문 추가함
        List<MemoResponse> memos = (includeMemo && !"guest".equalsIgnoreCase(userId)) ?
                memoService.getMemosByRecipe(recipeVideoId, userId) :
                Collections.emptyList();

        return RecipeDetailResponse.from(recipe, isLiked, isCompleted, relatedVideos, memos);
    }

    // page 를 -> offset(건너뛸 개수)로 변환
    @Override
    public PageResponse<RecipeResponse.Status> getStatusRecipes(String userId, String status, int page, int size) {
        int offset = (page - 1) * size;

        List<Recipe> recipes;
        int totalItems;

        switch (status.toLowerCase()) {
            case "completed" -> {
                recipes = recipeMapper.selectCompletedRecipes(userId, size, offset);
                totalItems = recipeMapper.countCompletedRecipes(userId);
            }
            case "liked" -> {
                recipes = recipeMapper.selectLikedRecipes(userId, size, offset);
                totalItems = recipeMapper.countLikedRecipes(userId);
            }
            default -> {
                recipes = Collections.emptyList();
                totalItems = 0;
            }
        }

        List<RecipeResponse.Status> items = recipes.stream()
                .map(RecipeResponse.Status::from)
                .collect(Collectors.toList());

        return PageResponse.of(items, totalItems, page, size);

    }

    @Override
    public RecipeDashboardResponse getRecipeDashboard(String userId) {

        // 6개월
        int period = 6;
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startDate = now.minusMonths(period - 1);

        long completedCount = recipeMapper.countCompletedRecipes(userId);
        long likedCount = recipeMapper.countLikedRecipes(userId);

        // 원별 완료 통계 부분
        List<Map<String, Object>> monthlyCompletedRawData = recipeMapper.selectMonthlyCompletedStats(userId, startDate);

        Map<Integer, Long> monthlyCompletedMap = new HashMap<>();
        for (Map<String, Object> data : monthlyCompletedRawData) {
            int month = ((Number) data.get("month")).intValue();
            long count = ((Number) data.get("count")).longValue();
            monthlyCompletedMap.put(month, count);
        }

        List<RecipeDashboardResponse.MonthlyCount> monthlyCompletedStats = new ArrayList<>();

        for (int i = 0; i < period; i++) {
            LocalDateTime targetDate = startDate.plusMonths(i);
            int targetMonth = targetDate.getMonthValue(); //달(month) 숫자만 출력
            long count = monthlyCompletedMap.getOrDefault(targetMonth, 0L);

            monthlyCompletedStats.add(RecipeDashboardResponse.MonthlyCount.from(targetMonth, count));
        }

        // 카테고리별 좋아요 통계 부분
        List<Map<String, Object>> likedCategoryRawData = recipeMapper.selectLikedCategoryStats(userId);

        Map<String, Long> likedCategoryMap = new HashMap<>();
        for (Map<String, Object> data : likedCategoryRawData) {
            String categoryName = (String) data.get("categoryName");
            long count = ((Number) data.get("count")).longValue();
            likedCategoryMap.put(categoryName, count);
        }

        Map<String, Integer> likedCategoryPercent = new HashMap<>();

        for (RecipeCategory categoryEnum : RecipeCategory.values()) {
            long count = likedCategoryMap.getOrDefault(categoryEnum.getKrCategory(), 0L);

            int percent = 0;
            if (likedCount > 0) {
                percent = (int) ((count * 100.0) / likedCount);
            }

            likedCategoryPercent.put(categoryEnum.getKrCategory(), percent);
        }

        return RecipeDashboardResponse.from(
                completedCount,
                likedCount,
                period,
                monthlyCompletedStats,
                likedCategoryPercent
        );
    }
}