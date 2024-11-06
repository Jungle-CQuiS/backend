package meowKai.CQuiS_backend.dto.request;

import lombok.*;

import java.util.List;
import java.util.UUID;

@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class RequestUpdateUserWrongQuizzesDto {
    private UUID uuid;
    private List<Long> wrongQuizIds;
}
