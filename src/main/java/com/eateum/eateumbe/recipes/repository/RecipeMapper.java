package com.eateum.eateumbe.recipes.repository;

import com.eateum.eateumbe.recipes.domain.Recipe;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface RecipeMapper {

    // AI 추천 레시피
    List<Recipe> selectRecipesByIds(@Param("recipeVideoIds") List<Long> recipeVideoIds);

    // 15분
    List<Recipe> selectSpeedRecipes();

    // 인기 요리
    List<Recipe> selectPopularRecipes();

    // 레시피 상세 조회
    Recipe selectRecipeDetail(@Param("recipeVideoId") Long recipeVideoId);

    // 좋아요 여부 조회
    boolean selectIsLiked(@Param("recipeVideoId") Long recipeVideoId, @Param("userId") Long userId);

    // 완성 여부 조회
    boolean selectIsCompleted(@Param("recipeVideoId") Long recipeVideoId, @Param("userId") Long userId);

    // 추천 영상 조회
    List<Recipe> selectRelatedVideos(@Param("recipeVideoId") Long recipeVideoId, @Param("categoryId") Long categoryId);
}

