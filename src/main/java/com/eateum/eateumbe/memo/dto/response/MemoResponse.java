package com.eateum.eateumbe.memo.dto.response;

import com.eateum.eateumbe.memo.domain.Memo;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemoResponse {
    private Long memoId;
    private Long recipeVideoId;
    private String content;
    private String createdAt;

    public static MemoResponse from(Memo memo) {
        return MemoResponse.builder()
            .memoId(memo.getMemoId())
            .recipeVideoId(memo.getRecipeVideoId())
            .content(memo.getContent())
            .createdAt(memo.getCreatedAt())
            .build();
    }
}
