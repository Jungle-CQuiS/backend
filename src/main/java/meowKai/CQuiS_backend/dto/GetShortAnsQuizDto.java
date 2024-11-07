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
    private String englishAnswer;
    private String koreanAnswer;

    public static GetShortAnsQuizDto createDto(ShortAnsQuiz shortAnsQuiz) {
        Quiz mappedQuiz = shortAnsQuiz.getQuiz();
        return GetShortAnsQuizDto.builder()
                .categoryId(mappedQuiz.getCategory().getId())
                .quizId(mappedQuiz.getId())
                .categoryType(mappedQuiz.getCategory().getCategory())
                .name(mappedQuiz.getName())
                .englishAnswer(shortAnsQuiz.getEnglishAnswer())
                .koreanAnswer(shortAnsQuiz.getKoreanAnswer())
                .build();
    }
}
