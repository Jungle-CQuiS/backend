package meowKai.CQuiS_backend.dto.response;

import lombok.*;
import meowKai.CQuiS_backend.domain.GameStatus;
import meowKai.CQuiS_backend.domain.RoomUserTeam;

@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class ResponseIsGameoverDto {
    private RoomUserTeam teamColor;
    private GameStatus gameStatus;
}
