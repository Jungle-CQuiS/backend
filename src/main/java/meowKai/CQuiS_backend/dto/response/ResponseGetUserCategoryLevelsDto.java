package meowKai.CQuiS_backend.dto.response;

import lombok.*;

import java.util.List;

@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class ResponseGetUserCategoryLevelsDto {
    private List<CategoryLevelData> categoryLevels;

    @Builder
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    @ToString
    public static class CategoryLevelData {
        private String categoryName;
        private Integer categoryLevel;
    }
}
