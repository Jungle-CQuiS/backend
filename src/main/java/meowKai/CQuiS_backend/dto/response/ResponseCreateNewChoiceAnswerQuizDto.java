package meowKai.CQuiS_backend.dto.response;

import lombok.*;
import meowKai.CQuiS_backend.domain.CategoryType;

@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResponseCreateNewChoiceAnswerQuizDto {

    private Long quizId;
    private String name;
    private Integer answer;
    private CategoryType category;
}
