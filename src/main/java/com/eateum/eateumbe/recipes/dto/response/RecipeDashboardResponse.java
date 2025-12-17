package com.eateum.eateumbe.recipes.dto.response;

import com.eateum.eateumbe.global.constant.RecipeCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecipeDashboardResponse {

    private Long completedCount;
    private Long likedCount;
    private Integer period;

    private List<MonthlyCount> completedMonthlyStats;

    private Map<String, Integer> likedCategoryPercent;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MonthlyCount{
        private Integer month;
        private Long count;

        public static MonthlyCount from(Integer month, Long count) {
            return MonthlyCount.builder().month(month).count(count).build();
        }
    }

    public static RecipeDashboardResponse from(Long completedCount,
                                               Long likedCount,
                                               Integer period,
                                               List<MonthlyCount> monthlyStats,
                                               Map<String, Integer> dbCategoryMap) {
        return RecipeDashboardResponse.builder()
                .completedCount(completedCount)
                .likedCount(likedCount)
                .period(period)
                .completedMonthlyStats(monthlyStats)
                .likedCategoryPercent(convertToJsonMap(dbCategoryMap))
                .build();
    }

    private static Map<String, Integer> convertToJsonMap(Map<String, Integer> dbMap) {
        return dbMap.entrySet().stream()
                .collect(Collectors.toMap(
                        entry -> RecipeCategory.getRecipeCategory(entry.getKey()).getEnCategory(),
                        Map.Entry::getValue
                ));
    }
}
