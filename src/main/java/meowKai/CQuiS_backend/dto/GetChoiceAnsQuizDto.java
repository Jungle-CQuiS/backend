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

    public static GetChoiceAnsQuizDto createDto(Quiz foundQuiz, ChoiceAnsQuiz foundChoiceAnsQuiz) {
        return GetChoiceAnsQuizDto.builder()
                .categoryId(foundQuiz.getCategory().getId())
                .quizId(foundQuiz.getId())
                .categoryType(foundQuiz.getCategory().getCategory())
                .name(foundQuiz.getName())
                .choice1(foundChoiceAnsQuiz.getChoice1())
                .choice2(foundChoiceAnsQuiz.getChoice2())
                .choice3(foundChoiceAnsQuiz.getChoice3())
                .choice4(foundChoiceAnsQuiz.getChoice4())
                .answer(foundChoiceAnsQuiz.getAnswer())
                .build();
    }
}
