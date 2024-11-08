package meowKai.CQuiS_backend.dto.request;

import lombok.*;
import meowKai.CQuiS_backend.domain.CategoryType;
import meowKai.CQuiS_backend.domain.QuizType;

import java.util.List;
import java.util.UUID;

@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RequestCreateNewChoiceAnswerQuizDto {

    private UUID uuid;
    private List<NewChoiceAnswerQuizDto> quizList;

    @Builder
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    @ToString
    public static class NewChoiceAnswerQuizDto {
        private CategoryType category;
        private String name;
        private QuizType type;
        private String choice1;
        private String choice2;
        private String choice3;
        private String choice4;
        private Integer answer;
    }
}
