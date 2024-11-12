package meowKai.CQuiS_backend.dto;

import lombok.*;
import meowKai.CQuiS_backend.domain.CategoryType;
import meowKai.CQuiS_backend.domain.ChoiceAnsQuiz;
import meowKai.CQuiS_backend.domain.Quiz;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class GetWrongChoiceAnsQuizDto {

    private Long categoryId;
    private Long quizId;
    private CategoryType categoryType;
    private String name;
    private String choice1;
    private String choice2;
    private String choice3;
    private String choice4;
    private Integer answer;
    private String username;

    public static GetWrongChoiceAnsQuizDto createDto(Quiz quiz) {
        ChoiceAnsQuiz choiceAnsQuiz = quiz.getChoiceAnsQuiz();
        return GetWrongChoiceAnsQuizDto.builder()
                .categoryId(quiz.getCategory().getId())
                .quizId(quiz.getId())
                .categoryType(quiz.getCategory().getCategory())
                .name(quiz.getName())
                .choice1(choiceAnsQuiz.getChoice1())
                .choice2(choiceAnsQuiz.getChoice2())
                .choice3(choiceAnsQuiz.getChoice3())
                .choice4(choiceAnsQuiz.getChoice4())
                .answer(choiceAnsQuiz.getAnswer())
                .username(quiz.getUser().getUsername())
                .build();
    }
}
