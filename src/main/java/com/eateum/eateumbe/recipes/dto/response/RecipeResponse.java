package com.eateum.eateumbe.recipes.dto.response;

import com.eateum.eateumbe.recipes.domain.Recipe;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecipeResponse {

    private Long recipeVideoId;
    private String videoTitle;
    private String thumbnailUrl;
    private String videoUrl;
    private String duration;
    private Long viewCount;
    private List<String> items;

    public static RecipeResponse from(Recipe recipe) {
        return RecipeResponse.builder()
                .recipeVideoId(recipe.getRecipeVideoId())
                .videoTitle(recipe.getVideoTitle())
                .thumbnailUrl(recipe.getThumbnailUrl())
                .videoUrl(recipe.getVideoUrl())
                .duration(recipe.getDuration())
                .viewCount(parseViewCount(recipe.getViewCount()))
                .items(Collections.emptyList())
                .build();
    }

    // 조회수 부분 파싱
    private static Long parseViewCount(String viewCountStr) {
        if (viewCountStr == null) return 0L;
        try {
            return Long.parseLong(viewCountStr.replaceAll("[^0-9]", ""));
        } catch (NumberFormatException e) {
            return 0L;
        }
    }
}