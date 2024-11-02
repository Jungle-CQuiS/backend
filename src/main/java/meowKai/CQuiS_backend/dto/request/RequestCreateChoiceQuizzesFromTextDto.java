package meowKai.CQuiS_backend.dto.request;

import lombok.*;
import meowKai.CQuiS_backend.domain.CategoryType;
import meowKai.CQuiS_backend.domain.QuizType;

@Builder
@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class RequestCreateChoiceQuizzesFromTextDto {
    private String textData;
    private QuizType quizType;
    private CategoryType categoryType;
    private Integer quizCount;
}
