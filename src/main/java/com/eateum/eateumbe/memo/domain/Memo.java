package com.eateum.eateumbe.memo.domain;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Memo {
    private Long memoId;
    private Long recipeVideoId;
    private Long userId;
    private String content;
    private String createdAt;
    private String updatedAt;
}