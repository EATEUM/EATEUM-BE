package com.eateum.eateumbe.recipes.dto.request;
import lombok.*;

import java.util.List;


public class RecipeRequest {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Recommend{
        private List<String> selectedItems;
    }


}
