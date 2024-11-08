package meowKai.CQuiS_backend.dto.request;

import lombok.*;
import meowKai.CQuiS_backend.domain.QuizType;

import java.util.List;
import java.util.UUID;

@Builder
@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class RequestGetShortAnswerQuizzesDto {

    private List<Long> categoryIds;
    private Integer quizCount;
    private UUID uuid;
}
