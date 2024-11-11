package meowKai.CQuiS_backend.dto.response;

import lombok.*;
import meowKai.CQuiS_backend.domain.QuizType;

import java.util.List;

@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class ResponseSubmitTimeoutDto<T> {

    private QuizType quizType;
    private List<T> answerList;
}
