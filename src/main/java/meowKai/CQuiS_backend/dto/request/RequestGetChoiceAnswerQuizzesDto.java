package meowKai.CQuiS_backend.dto.request;

import lombok.*;

import java.util.List;
import java.util.UUID;

@Builder
@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class RequestGetChoiceAnswerQuizzesDto {

    private List<Long> categoryIds;
    private Integer quizCount;
    private UUID uuid;
}
