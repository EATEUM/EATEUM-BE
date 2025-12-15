package com.eateum.eateumbe.recipes.domain;

import lombok.*;

import java.util.List;

@Data
public class Recipe {

    private Long recipeVideoId;
    private String videoTitle;
    private String videoUrl;
    private String thumbnailUrl;
    private Long viewCount;
    private Long userViewCount;
    private String duration;

    private List<RecipeItem> items;
    private List<RecipeStep> steps;

    private Long categoryId; // 관련 영상 조회?? TODO : 수정하기

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecipeItem {
        private Long itemId;
        private String itemName;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecipeStep {
        private Integer stepNumber;
        private String stepTitle;
        private String content;
    }
}