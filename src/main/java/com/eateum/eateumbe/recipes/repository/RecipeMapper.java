package com.eateum.eateumbe.recipes.repository;

import com.eateum.eateumbe.recipes.domain.Recipe;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface RecipeMapper {
    List<Recipe> selectRecipesByIds(@Param("recipeVideoIds") List<Long> recipeVideoIds);

    List<Recipe> selectSpeedRecipes();
}

