package com.eateum.eateumbe.recipes.dto.response;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import java.util.List;

@Data
public class RecipeDetailResponse {

    // 1. Video Info & Description
    private Long recipeVideoId;
    private String videoTitle;
    private String videoUrl;
    private String duration;

    // 2. User Status (Service에서 채워짐)
    private boolean isLiked;
    private boolean isCompleted;

    // 3. Steps (1:N 관계)
    private List<StepItem> steps;

    // 4. Related Videos (Service에서 채워짐)
    private List<RelatedVideoItem> relatedVideos;

    // 5. Memos (include_memo=true일 때 Service에서 채워짐)
    private List<MemoItem> memos;

    // ==========================================================
    // Nested DTOs

    @Getter
    @Builder
    public static class StepItem {
        private Integer stepNumber;
        private String stepTitle;
        private String description;
    }

    @Getter
    @Builder
    public static class RelatedVideoItem {
        private Long recipeVideoId;
        private String videoTitle;
        private String thumbnailUrl;
        private String duration;
    }

    @Getter
    @Builder
    public static class MemoItem {
        private Long memoId;
        private String content;
        private String createdAt;
    }
}