package meowKai.CQuiS_backend.dto.request;

import lombok.*;
import meowKai.CQuiS_backend.domain.CategoryType;
import meowKai.CQuiS_backend.domain.QuizType;

@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RequestCreateNewShortAnswerQuizDto {

    private CategoryType category;
    private String name;
    private QuizType type;
    private String englishAnswer;
    private String koreanAnswer;
}