package meowKai.CQuiS_backend.dto.response;

import lombok.*;
import meowKai.CQuiS_backend.domain.CategoryType;

import java.util.List;

@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class ResponseUpdateUserCategoryLevelsDto {
    private List<CategoryLevelData> categoryLevels;

    @Builder
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    @ToString
    public static class CategoryLevelData {
        private CategoryType categoryName;
        private Integer categoryLevel;
    }
}
