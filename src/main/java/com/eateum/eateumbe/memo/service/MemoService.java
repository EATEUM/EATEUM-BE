package com.eateum.eateumbe.memo.service;

import com.eateum.eateumbe.memo.dto.request.MemoRequest;
import com.eateum.eateumbe.memo.dto.response.MemoResponse;

import java.util.List;

public interface MemoService {

    List<MemoResponse> getMemosByRecipe(Long recipeVideoId, String userId);

    MemoResponse createMemo(Long recipeVideoId, String userId, MemoRequest request);

    void deleteMemo(Long recipeVideoId, String userId);

}
