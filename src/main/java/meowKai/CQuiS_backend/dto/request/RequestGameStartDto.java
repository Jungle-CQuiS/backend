package meowKai.CQuiS_backend.dto.request;

import lombok.*;
import meowKai.CQuiS_backend.domain.GameStatus;

@Builder
@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class RequestGameStartDto {

    private Long roomId;
    private Long roomUserId;
    private GameStatus gameStatus;
}
