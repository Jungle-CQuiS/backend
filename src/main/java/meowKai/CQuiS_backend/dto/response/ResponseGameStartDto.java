package meowKai.CQuiS_backend.dto.response;

import lombok.*;
import meowKai.CQuiS_backend.domain.RoomUserTeam;

@Builder
@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class ResponseGameStartDto {

    private RoomUserTeam teamColor;
}
