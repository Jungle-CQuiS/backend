package meowKai.CQuiS_backend.dto.request;

import lombok.*;

import java.util.List;

@Builder
@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class RequestGetChoiceAnswerQuizzesDto {

    private List<Long> categoryIds;
    private Integer quizCount;
}
