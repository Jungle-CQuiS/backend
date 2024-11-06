package meowKai.CQuiS_backend.dto.response;

import lombok.*;
import meowKai.CQuiS_backend.domain.GameStatus;
import meowKai.CQuiS_backend.domain.ResponseStatus;
import meowKai.CQuiS_backend.domain.RoomUserTeam;

@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class ResponseIsGameoverDto {
    private ResponseStatus responseStatus;
    private RoomUserTeam teamColor;
    private GameStatus gameStatus;
}
