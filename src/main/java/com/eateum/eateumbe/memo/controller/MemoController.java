package com.eateum.eateumbe.memo.controller;

import com.eateum.eateumbe.global.common.ApiResponse;
import com.eateum.eateumbe.global.common.BaseController;
import com.eateum.eateumbe.memo.dto.request.MemoRequest;
import com.eateum.eateumbe.memo.dto.response.MemoResponse;
import com.eateum.eateumbe.memo.service.MemoService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/recipes/{recipe_video_id}/memo")
@RequiredArgsConstructor
public class MemoController extends BaseController {

    private final MemoService memoService;

    @Operation(summary = "메모 조회")
    @GetMapping
    public ApiResponse<List<MemoResponse>> getMemosByRecipe(
            @AuthenticationPrincipal String userId,
            @PathVariable("recipe_video_id") Long recipeVideoId) {
        String safeUserId = requireAuth(userId);
        List<MemoResponse> response = memoService.getMemosByRecipe(recipeVideoId, safeUserId);

        return ApiResponse.success(response);
    }

    @Operation(summary = "메모 등록")
    @PostMapping
    public ApiResponse<MemoResponse> createMemo(
            @AuthenticationPrincipal String userId,
            @PathVariable("recipe_video_id") Long recipeVideoId,
            @RequestBody MemoRequest request) {
        String safeUserId = requireAuth(userId);
        MemoResponse response = memoService.createMemo(recipeVideoId, safeUserId, request);
        return ApiResponse.success(response);
    }

    @Operation(summary = "메모 삭제")
    @DeleteMapping("/{memo_id}")
    public ApiResponse<Void> deleteMemo(
            @AuthenticationPrincipal String userId,
            @PathVariable("recipe_video_id") Long recipeVideoId,
            @PathVariable("memo_id") Long memoId) {
        String safeUserId = requireAuth(userId);
        memoService.deleteMemo(memoId, safeUserId);
        return ApiResponse.success(null);
    }
}
