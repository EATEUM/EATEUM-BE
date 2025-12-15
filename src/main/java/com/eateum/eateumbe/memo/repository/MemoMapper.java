package com.eateum.eateumbe.memo.repository;

import com.eateum.eateumbe.memo.domain.Memo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface MemoMapper {
    List<Memo> selectMemosByRecipe(@Param("recipeVideoId") Long recipeVideoId, @Param("userId") Long userId);

    void addMemo(Memo memo);

    Memo selectMemoById(@Param("memoId") Long memoId);

    void deleteMemoById(@Param("memoId") Long memoId, @Param("userId") Long userId);
}
