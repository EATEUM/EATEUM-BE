package com.eateum.eateumbe.memo.controller;

import com.eateum.eateumbe.global.common.ApiResponse;
import com.eateum.eateumbe.memo.dto.request.MemoRequest;
import com.eateum.eateumbe.memo.dto.response.MemoResponse;
import com.eateum.eateumbe.memo.service.MemoService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/recipes/{recipe_video_id}/memo")
@RequiredArgsConstructor
public class MemoController {

    private final MemoService memoService;

    private Long getCurrentUserId() {
        // TODO: 실제 구현 시 Spring Security 등에서 인증된 유저 ID를 가져오도록 변경해야 합니다.
        return 1L; // 임시 테스트용
    }

    @GetMapping
    public ApiResponse<List<MemoResponse>> getMemosByRecipe(
        @PathVariable("recipe_video_id") Long recipeVideoId
    ) {
        Long userId = getCurrentUserId();

        List<MemoResponse> response = memoService.getMemosByRecipe(recipeVideoId, userId);

        return ApiResponse.success(response);
    }

    @PostMapping
    public ApiResponse<MemoResponse> createMemo(
        @PathVariable("recipe_video_id") Long recipeVideoId,
        @RequestBody MemoRequest request
    ) {
        Long userId = getCurrentUserId();
        MemoResponse response = memoService.createMemo(recipeVideoId, userId, request);
        return ApiResponse.success(response);
    }

    @DeleteMapping("/{memo_id}")
    public ApiResponse<Void> deleteMemo(
        @PathVariable("recipe_video_id") Long recipeVideoId,
        @PathVariable("memo_id") Long memoId
    ) {
        Long userId = getCurrentUserId();
        memoService.deleteMemo(memoId, userId);
        return ApiResponse.success(null);
    }

}
