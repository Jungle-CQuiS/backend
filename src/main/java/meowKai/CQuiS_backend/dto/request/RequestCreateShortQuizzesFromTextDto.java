package meowKai.CQuiS_backend.dto.request;

import lombok.*;
import meowKai.CQuiS_backend.domain.CategoryType;
import meowKai.CQuiS_backend.domain.QuizType;

@ToString
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
public class RequestCreateShortQuizzesFromTextDto {
    private String textData;
    private Integer quizCount;
}