package meowKai.CQuiS_backend.dto.response;

import lombok.*;
import meowKai.CQuiS_backend.domain.CategoryType;

@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResponseCreateNewShortAnswerQuizDto {

    private Long quizId;
    private String name;
    private String englishAnswer;
    private String koreanAnswer;
    private CategoryType category;
}
