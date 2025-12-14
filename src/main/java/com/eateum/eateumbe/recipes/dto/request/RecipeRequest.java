package com.eateum.eateumbe.recipes.dto.request;
import lombok.Data;

import java.util.List;

@Data
public class RecipeRequest {
    private List<String> selectedItems;
}
