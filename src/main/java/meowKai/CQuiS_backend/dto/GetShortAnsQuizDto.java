package meowKai.CQuiS_backend.dto;

import lombok.*;
import meowKai.CQuiS_backend.domain.CategoryType;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Getter
public class GetShortAnsQuizDto {

    private Long categoryId;
    private Long quizId;
    private CategoryType categoryType;
    private String name;
    private String englishAnswer;
    private String koreanAnswer;
}
