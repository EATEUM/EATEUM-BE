package com.eateum.eateumbe.recipes.repository;

import com.eateum.eateumbe.recipes.domain.Recipe;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface RecipeMapper {

    // AI 추천 레시피
    List<Recipe> selectRecipesByIds(@Param("recipeVideoIds") List<Long> recipeVideoIds);

    // 15분
    List<Recipe> selectSpeedRecipes();

    // 인기 요리
    List<Recipe> selectPopularRecipes();

    // 레시피 상세 조회
    Recipe selectRecipeDetail(@Param("recipeVideoId") Long recipeVideoId, @Param("userId") String userId);

    // 좋아요 여부 조회
    Boolean selectIsLiked(@Param("recipeVideoId") Long recipeVideoId, @Param("userId") String userId);

    // 완성 여부 조회
    Boolean selectIsCompleted(@Param("recipeVideoId") Long recipeVideoId, @Param("userId") String userId);

    // 추천 영상 조회
    List<Recipe> selectRelatedVideos(@Param("recipeVideoId") Long recipeVideoId, @Param("categoryId") Long categoryId);

    // Complete
    // 완성 등록
    void insertComplete(@Param("userId") String userId, @Param("recipeVideoId") Long recipeVideoId);

    // 완성 취소
    void deleteComplete(@Param("userId") String userId, @Param("recipeVideoId") Long recipeVideoId);

    // 완성한 레시피 목록 조히
    List<Recipe> selectCompletedRecipes(@Param("userId") String userId, @Param("size") int size, @Param("offset") int offset);

    // 완성한 레시피 갯수 조회
    int countCompletedRecipes(@Param("userId") String userId);

    // Liked
    // 좋아요 등록
    void insertLike(@Param("userId") String userId, @Param("recipeVideoId") Long recipeVideoId);

    // 좋아요 취소
    void deleteLike(@Param("userId") String userId, @Param("recipeVideoId") Long recipeVideoId);

    // 좋아요 레시피 목록 조히
    List<Recipe> selectLikedRecipes(@Param("userId") String userId, @Param("size") int size, @Param("offset") int offset);

    // 좋아요 레시피 갯수 조회
    int countLikedRecipes(@Param("userId") String userId);

    // 6개월 완성 레시피 통게
    List<Map<String, Object>> selectMonthlyCompletedStats(@Param("userId") String userId, @Param("startDate") LocalDateTime startDate);

    // 좋아요 카테고리 비율 통계
    List<Map<String, Object>> selectLikedCategoryStats(@Param("userId") String userId);

    // 시스템 내부 조회수 증가
    void updateUserViewCount(@Param("recipeVideoId") Long recipeVideoId);
}

