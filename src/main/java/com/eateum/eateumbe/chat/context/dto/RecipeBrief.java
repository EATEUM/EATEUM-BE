package com.eateum.eateumbe.chat.context.dto;

import lombok.Getter;

/**
 * 완성 / 좋아요 레시피 공통
 */
@Getter
public class RecipeBrief {

    private Long recipeVideoId;
    private String videoTitle;
    private String categoryName;

}
