package meowKai.CQuiS_backend.dto.request;

import lombok.*;

import java.util.List;
import java.util.UUID;

@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class RequestGetMixAnswerQuizzesDto {

    private List<Long> categoryIds;
    private Integer quizCount;
    private UUID uuid;
}
