package meowKai.CQuiS_backend.dto;

import lombok.*;
import meowKai.CQuiS_backend.domain.CategoryType;
import meowKai.CQuiS_backend.domain.ChoiceAnsQuiz;
import meowKai.CQuiS_backend.domain.Quiz;
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
    private String username;

    public static GetRandomChoiceQuizDto createDto(Quiz quiz) {
        ChoiceAnsQuiz choiceAnsQuiz = quiz.getChoiceAnsQuiz();
        return GetRandomChoiceQuizDto.builder()
                .quizId(quiz.getId())
                .quizType(QuizType.CHOICE)
                .categoryId(quiz.getCategory().getId())
                .categoryType(quiz.getCategory().getCategory())
                .name(quiz.getName())
                .choice1(choiceAnsQuiz.getChoice1())
                .choice2(choiceAnsQuiz.getChoice2())
                .choice3(choiceAnsQuiz.getChoice3())
                .choice4(choiceAnsQuiz.getChoice4())
                .username(quiz.getUser().getUsername())
                .build();
    }
}
