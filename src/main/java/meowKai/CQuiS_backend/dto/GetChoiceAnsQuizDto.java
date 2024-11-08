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
public class GetChoiceAnsQuizDto {

    private Long categoryId;
    private Long quizId;
    private CategoryType categoryType;
    private String name;
    private String choice1;
    private String choice2;
    private String choice3;
    private String choice4;
    private Integer answer;

    public static GetChoiceAnsQuizDto createDto(ChoiceAnsQuiz choiceAnsQuiz) {
        Quiz mappedQuiz = choiceAnsQuiz.getQuiz();
        return GetChoiceAnsQuizDto.builder()
                .categoryId(mappedQuiz.getCategory().getId())
                .quizId(mappedQuiz.getId())
                .categoryType(mappedQuiz.getCategory().getCategory())
                .name(mappedQuiz.getName())
                .choice1(choiceAnsQuiz.getChoice1())
                .choice2(choiceAnsQuiz.getChoice2())
                .choice3(choiceAnsQuiz.getChoice3())
                .choice4(choiceAnsQuiz.getChoice4())
                .answer(choiceAnsQuiz.getAnswer())
                .build();
    }
}
