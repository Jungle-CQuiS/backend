package meowKai.CQuiS_backend.dto.request;

import lombok.*;

@Builder
@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class RequestCreateChoiceQuizzesFromTextDto {
    private String textData;
    private Integer quizCount;
}
