package meowKai.CQuiS_backend.dto.request;

import lombok.*;
import meowKai.CQuiS_backend.domain.QuizType;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class RequestGradeDto {

    private Long quizId;
    private String userInput;
}