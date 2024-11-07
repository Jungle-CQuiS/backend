package meowKai.CQuiS_backend.dto;

import lombok.*;
import meowKai.CQuiS_backend.domain.CategoryType;
import meowKai.CQuiS_backend.domain.ChoiceAnsQuiz;
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

    public static GetRandomChoiceQuizDto createDto(ChoiceAnsQuiz choiceAnsQuiz) {
        return GetRandomChoiceQuizDto.builder()
                .quizId(choiceAnsQuiz.getQuiz().getId())
                .quizType(QuizType.CHOICE)
                .categoryId(choiceAnsQuiz.getQuiz().getCategory().getId())
                .categoryType(choiceAnsQuiz.getQuiz().getCategory().getCategory())
                .name(choiceAnsQuiz.getQuiz().getName())
                .choice1(choiceAnsQuiz.getChoice1())
                .choice2(choiceAnsQuiz.getChoice2())
                .choice3(choiceAnsQuiz.getChoice3())
                .choice4(choiceAnsQuiz.getChoice4())
                .choiceAnswer(choiceAnsQuiz.getAnswer())
                .build();
    }
}
