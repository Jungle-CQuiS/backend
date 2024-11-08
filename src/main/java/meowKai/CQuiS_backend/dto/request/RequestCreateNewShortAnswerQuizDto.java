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
public class RequestCreateNewShortAnswerQuizDto {

    private UUID uuid;
    private List<NewShortAnswerQuizDto> quizList;

    @Builder
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    @ToString
    public static class NewShortAnswerQuizDto {
        private CategoryType categoryType;
        private String name;
        private QuizType type;
        private String englishAnswer;
        private String koreanAnswer;
    }
}