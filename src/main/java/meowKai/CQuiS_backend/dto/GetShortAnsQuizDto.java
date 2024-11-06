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
    private CategoryType categoryName;
    private String name;
    private String englishAnswer;
    private String koreanAnswer;

    public static GetShortAnsQuizDto createDto(Quiz foundQuiz, ShortAnsQuiz foundShortAnsQuiz) {
        return GetShortAnsQuizDto.builder()
                .categoryId(foundQuiz.getCategory().getId())
                .quizId(foundQuiz.getId())
                .categoryName(foundQuiz.getCategory().getCategory())
                .name(foundQuiz.getName())
                .englishAnswer(foundShortAnsQuiz.getEnglishAnswer())
                .koreanAnswer(foundShortAnsQuiz.getKoreanAnswer())
                .build();
    }
}
