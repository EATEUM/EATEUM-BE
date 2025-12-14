package com.eateum.eateumbe.recipes.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Recipe {

    private Long recipeVideoId;
    private String videoTitle;
    private String videoUrl;
    private String thumbnailUrl;
    private String viewCount;
    private String duration;
    // 추후 수정해야 함
    private String recipeJson;

    private List<RecipeItem> items;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecipeItem {
        private Long itemId;
        private String itemName;
    }
}