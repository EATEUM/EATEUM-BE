package com.eateum.eateumbe.memo.controller;

import com.eateum.eateumbe.global.common.ApiResponse;
import com.eateum.eateumbe.memo.dto.response.MemoResponse;
import com.eateum.eateumbe.memo.repository.MemoMapper;
import com.eateum.eateumbe.memo.service.MemoService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/recipes/{recipe_video_id}/memo")
@RequiredArgsConstructor
public class MemoController {

    private final MemoService memoService;


    // 💡 임시 유저 ID 획득 메서드 (실제로는 Security Context에서 가져와야 함)
    private Long getCurrentUserId() {
        // TODO: 실제 구현 시 Spring Security 등에서 인증된 유저 ID를 가져오도록 변경해야 합니다.
        return 1L; // 임시 테스트용
    }

    @GetMapping
    public ApiResponse<List<MemoResponse>> getMemosByRecipe(
        @PathVariable("recipe_video_id") Long recipeVideoId
    ) {
        // 🌟 1. 컨트롤러에서 유저 ID 획득
        Long userId = getCurrentUserId();

        // 🌟 2. 획득한 userId를 Service에 전달하여 호출
        List<MemoResponse> response = memoService.getMemosByRecipe(recipeVideoId, userId);

        return ApiResponse.success(response);
    }

}
