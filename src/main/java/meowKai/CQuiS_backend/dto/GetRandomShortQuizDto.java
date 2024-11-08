package meowKai.CQuiS_backend.dto;

import lombok.*;
import meowKai.CQuiS_backend.domain.CategoryType;
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
    private String shortKoreanAnswer;
    private String shortEnglishAnswer;

    public static GetRandomShortQuizDto createDto(ShortAnsQuiz shortAnsQuiz) {
        return GetRandomShortQuizDto.builder()
                .quizId(shortAnsQuiz.getQuiz().getId())
                .quizType(QuizType.SHORT)
                .categoryId(shortAnsQuiz.getQuiz().getCategory().getId())
                .categoryType(shortAnsQuiz.getQuiz().getCategory().getCategory())
                .name(shortAnsQuiz.getQuiz().getName())
                .shortKoreanAnswer(shortAnsQuiz.getKoreanAnswer())
                .shortEnglishAnswer(shortAnsQuiz.getEnglishAnswer())
                .build();
    }
}
