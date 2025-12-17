package com.eateum.eateumbe.global.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

@Getter
@AllArgsConstructor
public enum RecipeCategory {

    KOREAN("korean", "한식"),
    WESTERN("western", "양식"),
    JAPANESE("japanese", "일식"),
    CHINESE("chinese", "중식"),
    BUNSIK("bunsik", "분식");

    private final String enCategory; //JSON
    private final String krCategory; //DB

    public static RecipeCategory getRecipeCategory(String krCategory) {
        return Arrays.stream(RecipeCategory.values())
                .filter(category -> category.krCategory.equals(krCategory))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("해당되는 레시피 카테고리 없음")); // 해당 카테고리 아니면 예외 던지는 것으로
    }
}
