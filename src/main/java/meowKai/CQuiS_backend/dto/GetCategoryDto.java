package meowKai.CQuiS_backend.dto;

import lombok.*;
import meowKai.CQuiS_backend.domain.CategoryType;

@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GetCategoryDto {

    private Long categoryId;
    private CategoryType categoryType;
}
