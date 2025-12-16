package com.eateum.eateumbe.global.common;

import lombok.Builder;
import lombok.Getter;
import java.util.List;

@Getter
@Builder
public class PageResponse<T> {

    // 범용성을 위해 items로 통일 기존 api 명세서 재료 조회 fridgeitems, 완성.좋아요 recipeVideo -> items 로 통일
    private List<T> items;
    private int totalItems;
    private int page; // 현재 페이지 번호
    private int size; // 페이당 개수

    public static <T> PageResponse<T> of(List<T> items, int totalItems, int page, int size) {

        return PageResponse.<T>builder()
                .items(items)
                .totalItems(totalItems)
                .page(page)
                .size(size)
                .build();
    }
}
