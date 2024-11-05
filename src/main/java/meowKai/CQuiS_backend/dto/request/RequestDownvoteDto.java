package meowKai.CQuiS_backend.dto.request;

import lombok.*;

@Builder
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class RequestDownvoteDto {
    private Long quizId;
}
