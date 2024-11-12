package meowKai.CQuiS_backend.dto;

import lombok.*;
import meowKai.CQuiS_backend.domain.CategoryType;
import meowKai.CQuiS_backend.domain.Quiz;
import meowKai.CQuiS_backend.domain.QuizType;
import meowKai.CQuiS_backend.domain.ShortAnsQuiz;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class GetRandomShortQuizDto {

    private Long quizId;
    private QuizType quizType;
    private Long categoryId;
    private CategoryType categoryType;
    private String name;
    private String username;

    public static GetRandomShortQuizDto createDto(Quiz quiz) {
        ShortAnsQuiz shortAnsQuiz = quiz.getShortAnsQuiz();
        return GetRandomShortQuizDto.builder()
                .quizId(quiz.getId())
                .quizType(QuizType.SHORT)
                .categoryId(quiz.getCategory().getId())
                .categoryType(quiz.getCategory().getCategory())
                .name(shortAnsQuiz.getQuiz().getName())
                .username(quiz.getUser().getUsername())
                .build();
    }
}
