package com.eateum.eateumbe.memo.service;

import com.eateum.eateumbe.memo.domain.Memo;
import com.eateum.eateumbe.memo.dto.request.MemoRequest;
import com.eateum.eateumbe.memo.dto.response.MemoResponse;
import com.eateum.eateumbe.memo.repository.MemoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemoServiceImpl implements MemoService {

    private final MemoMapper memoMapper;
    // TODO : 유저 서비스 완료시, 추가해야 함

    @Override
    public List<MemoResponse> getMemosByRecipe(Long recipeVideoId, Long userId) {

        List<Memo> memos = memoMapper.selectMemosByRecipe(recipeVideoId, userId);

        // 아직 아무것도 작성되지 않을 경우에도 null이 아닌 빈 리스트[] 반환하도록 설정
        if (memos == null) {
        return Collections.emptyList();
        }

        return memos.stream()
            .map(MemoResponse::from)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public MemoResponse createMemo(Long recipeVideoId, Long userId, MemoRequest request) {

        Memo memo = Memo.builder()
                .recipeVideoId(recipeVideoId)
                .userId(userId)
                .content(request.getContent())
                .build();

        memoMapper.addMemo(memo);

        Memo saveMemo = memoMapper.selectMemoById(memo.getMemoId());

        return MemoResponse.from(saveMemo);
    }

    @Override
    @Transactional
    public void deleteMemo(Long memoId, Long userId) {
        memoMapper.deleteMemoById(memoId, userId);
    }
}
