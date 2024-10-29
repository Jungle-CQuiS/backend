package meowKai.CQuiS_backend.dto.request;

import lombok.*;
import meowKai.CQuiS_backend.domain.CategoryType;
import meowKai.CQuiS_backend.domain.QuizType;

@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RequestCreateNewChoiceAnswerQuiz {

    private CategoryType category;
    private String name;
    private QuizType type;
    private String choice1;
    private String choice2;
    private String choice3;
    private String choice4;
    private Integer answer;
}
