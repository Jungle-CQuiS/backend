package meowKai.CQuiS_backend.dto.response;

import lombok.*;
import lombok.experimental.SuperBuilder;
import meowKai.CQuiS_backend.domain.CategoryType;

@SuperBuilder
@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public abstract class ResponseSelectQuizDto {
    private Long quizId;
    private String name;
    private CategoryType categoryType;
}

