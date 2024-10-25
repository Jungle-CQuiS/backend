package meowKai.CQuiS_backend.dto.response;

import lombok.*;
import meowKai.CQuiS_backend.domain.RoomUserTeam;

@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResponseSwitchTeamDto {

    private Long roomUserId;
    private RoomUserTeam team;
}
