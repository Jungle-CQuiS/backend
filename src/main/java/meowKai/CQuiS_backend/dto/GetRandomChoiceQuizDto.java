package meowKai.CQuiS_backend.dto;

import lombok.*;
import meowKai.CQuiS_backend.domain.CategoryType;
import meowKai.CQuiS_backend.domain.QuizType;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class GetRandomChoiceQuizDto {

    private Long quizId;
    private QuizType quizType;
    private Long categoryId;
    private CategoryType categoryType;
    private String name;
    private String choice1;
    private String choice2;
    private String choice3;
    private String choice4;
    private Integer choiceAnswer;
}
