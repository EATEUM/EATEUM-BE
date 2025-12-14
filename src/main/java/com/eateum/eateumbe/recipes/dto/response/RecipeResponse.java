package com.eateum.eateumbe.recipes.dto.response;

import com.eateum.eateumbe.recipes.domain.Recipe;
import lombok.*;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class RecipeResponse {

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecipeItem {
        private Long itemId;
        private String itemName;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Recommend {
        private Long recipeVideoId;
        private String videoTitle;
        private String thumbnailUrl;
        private String videoUrl;
        private String duration;
        private Long viewCount;

        private List<RecipeItem> items;

        public static Recommend from(Recipe recipe) {
            List<RecipeItem> responseItems = Collections.emptyList();

            if (recipe.getItems() != null) {
                responseItems = recipe.getItems().stream()
                        .map(item -> RecipeItem.builder()
                                .itemId(item.getItemId())
                                .itemName(item.getItemName())
                                .build())
                        .collect(Collectors.toList());
            }

            return Recommend.builder()
                    .recipeVideoId(recipe.getRecipeVideoId())
                    .videoTitle(recipe.getVideoTitle())
                    .thumbnailUrl(recipe.getThumbnailUrl())
                    .videoUrl(recipe.getVideoUrl())
                    .duration(recipe.getDuration())
                    .viewCount(parseViewCount(recipe.getViewCount()))
                    .items(responseItems)
                    .build();
        }

        // 조회수 파싱 헬퍼 메서드
        private static Long parseViewCount(String viewCountStr) {
            if (viewCountStr == null) return 0L;
            try {
                return Long.parseLong(viewCountStr.replaceAll("[^0-9]", ""));
            } catch (NumberFormatException e) {
                return 0L;
            }
        }
    }
}