package meowKai.CQuiS_backend.dto;

import lombok.*;
import meowKai.CQuiS_backend.domain.CategoryType;
import meowKai.CQuiS_backend.domain.QuizType;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class GetRandomShortQuizDto {

    private Long quizId;
    private QuizType quizType;
    private Long categoryId;
    private CategoryType categoryType;
    private String name;
    private String shortKoreanAnswer;
    private String shortEnglishAnswer;
}
