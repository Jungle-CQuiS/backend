package meowKai.CQuiS_backend.dto;

import lombok.*;
import meowKai.CQuiS_backend.domain.CategoryType;
import meowKai.CQuiS_backend.domain.Quiz;
import meowKai.CQuiS_backend.domain.ShortAnsQuiz;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Getter
public class GetShortAnsQuizDto {

    private Long categoryId;
    private Long quizId;
    private CategoryType categoryType;
    private String name;
    private String username;

    public static GetShortAnsQuizDto createDto(Quiz quiz) {
        return GetShortAnsQuizDto.builder()
                .categoryId(quiz.getCategory().getId())
                .quizId(quiz.getId())
                .categoryType(quiz.getCategory().getCategory())
                .name(quiz.getName())
                .username(quiz.getUser().getUsername())
                .build();
    }
}
