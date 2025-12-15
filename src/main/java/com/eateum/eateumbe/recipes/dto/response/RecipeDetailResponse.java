package com.eateum.eateumbe.recipes.dto.response;

import com.eateum.eateumbe.memo.dto.response.MemoResponse;
import com.eateum.eateumbe.recipes.domain.Recipe;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
public class RecipeDetailResponse {

    private Long recipeVideoId;
    private String videoTitle;
    private String videoUrl;
    private String duration;

    private Boolean isLiked;
    private Boolean isCompleted;

    private List<StepItem> steps;

    private List<RelatedVideoItem> relatedVideos;

    private List<MemoResponse> memos;

    @Getter
    @Builder
    public static class StepItem {
        private Integer stepNumber;
        private String stepTitle;
        private String content;

        public static StepItem from(Recipe.RecipeStep step) {
            return StepItem.builder()
                .stepNumber(step.getStepNumber())
                .stepTitle(step.getStepTitle())
                .content(step.getContent())
                .build();
        }
    }

    @Getter
    @Builder
    public static class RelatedVideoItem {
        private Long recipeVideoId;
        private String videoTitle;
        private String thumbnailUrl;
        private String duration;

        public static RelatedVideoItem from(Recipe recipe) {
            return RelatedVideoItem.builder()
                .recipeVideoId(recipe.getRecipeVideoId())
                .videoTitle(recipe.getVideoTitle())
                .thumbnailUrl(recipe.getThumbnailUrl())
                .duration(recipe.getDuration())
                .build();
        }
    }

    public static RecipeDetailResponse from(
            Recipe recipe,
            Boolean isLiked,
            Boolean isCompleted,
            List<Recipe> relatedVideos,
            // MemoService에서 이미 DTO로 변환되어 전달된 리스트를 받음!!
            // 왜? 다른 서비스 즉 MemoService 랑 주고 받을때는 완성된 결과물(DTO)만 주고 받는 것이 원칙이라 함!
            List<MemoResponse> memos) {

        List<StepItem> stepItems = recipe.getSteps() != null ?
            recipe.getSteps().stream().map(StepItem::from).collect(Collectors.toList()) : List.of();

        List<RelatedVideoItem> relatedVideoItems = relatedVideos.stream()
            .map(RelatedVideoItem::from)
            .collect(Collectors.toList());

        return RecipeDetailResponse.builder()
            .recipeVideoId(recipe.getRecipeVideoId())
            .videoTitle(recipe.getVideoTitle())
            .videoUrl(recipe.getVideoUrl())
            .duration(recipe.getDuration())
            .isLiked(isLiked)
            .isCompleted(isCompleted)
            .steps(stepItems)
            .relatedVideos(relatedVideoItems)
            .memos(memos)
            .build();
    }
}