package meowKai.CQuiS_backend.dto.request;

import lombok.*;

import java.util.UUID;

@Builder
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class RequestDownvoteDto {
    private Long quizId;
    private UUID uuid;
}
