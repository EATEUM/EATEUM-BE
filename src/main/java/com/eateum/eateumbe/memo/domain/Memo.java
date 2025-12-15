package com.eateum.eateumbe.memo.domain;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Memo {
    private Long memoId;
    private Long recipeVideoId;
    private Long userId;
    private String content;
    private String createdAt;
    private String updatedAt;
}