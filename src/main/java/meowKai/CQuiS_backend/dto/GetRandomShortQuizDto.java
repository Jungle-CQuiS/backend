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
    private String shortKoreanAnswer;
    private String shortEnglishAnswer;
    private String username;

    public static GetRandomShortQuizDto createDto(ShortAnsQuiz shortAnsQuiz) {
        Quiz mappedQuiz = shortAnsQuiz.getQuiz();
        return GetRandomShortQuizDto.builder()
                .quizId(mappedQuiz.getId())
                .quizType(QuizType.SHORT)
                .categoryId(mappedQuiz.getCategory().getId())
                .categoryType(mappedQuiz.getCategory().getCategory())
                .name(shortAnsQuiz.getQuiz().getName())
                .shortKoreanAnswer(shortAnsQuiz.getKoreanAnswer())
                .shortEnglishAnswer(shortAnsQuiz.getEnglishAnswer())
                .username(mappedQuiz.getUser().getUsername())
                .build();
    }
}
