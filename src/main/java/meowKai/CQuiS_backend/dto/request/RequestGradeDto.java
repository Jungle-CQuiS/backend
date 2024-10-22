package meowKai.CQuiS_backend.dto.request;

import lombok.*;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class RequestGradeDto {

    private Integer categoryId;
    private Integer quizId;
    private String userInput;
}
