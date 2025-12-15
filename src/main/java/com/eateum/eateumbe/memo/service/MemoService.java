package com.eateum.eateumbe.memo.service;

import com.eateum.eateumbe.memo.dto.request.MemoRequest;
import com.eateum.eateumbe.memo.dto.response.MemoResponse;

import java.util.List;

public interface MemoService {

    List<MemoResponse> getMemosByRecipe(Long recipeVideoId, Long userId);

    MemoResponse createMemo(Long recipeVideoId, Long userId, MemoRequest request);


}
