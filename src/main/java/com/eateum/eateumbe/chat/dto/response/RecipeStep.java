package com.eateum.eateumbe.chat.dto.response;

import lombok.Data;

/**
 * 레시피 단계 정보 DTO
 * - AI가 요리 맥락을 이해하기 위한 참고 데이터
 */
@Data
public class RecipeStep {

    private Long stepId;
    private Long recipeVideoId;
    private Integer stepNumber;
    private String stepTitle;
    private String content;

}
