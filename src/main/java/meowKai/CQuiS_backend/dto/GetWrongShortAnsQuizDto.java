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
public class GetWrongShortAnsQuizDto {

    private Long categoryId;
    private Long quizId;
    private CategoryType categoryType;
    private String name;
    private String englishAnswer;
    private String koreanAnswer;
    private String username;

    public static GetWrongShortAnsQuizDto createDto(Quiz quiz) {
        ShortAnsQuiz shortAnsQuiz = quiz.getShortAnsQuiz();
        return GetWrongShortAnsQuizDto.builder()
                .categoryId(quiz.getCategory().getId())
                .quizId(quiz.getId())
                .categoryType(quiz.getCategory().getCategory())
                .name(quiz.getName())
                .englishAnswer(shortAnsQuiz.getEnglishAnswer())
                .koreanAnswer(shortAnsQuiz.getKoreanAnswer())
                .username(quiz.getUser().getUsername())
                .build();
    }
}
