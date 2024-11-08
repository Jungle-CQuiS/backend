package meowKai.CQuiS_backend.dto.response;

import lombok.*;
import meowKai.CQuiS_backend.domain.RoomUser;
import meowKai.CQuiS_backend.domain.RoomUserTeam;

@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResponseSwitchTeamDto {

    private Long roomUserId;
    private RoomUserTeam team;

    public static ResponseSwitchTeamDto createDto(RoomUser roomUser) {
        return ResponseSwitchTeamDto.builder()
                .roomUserId(roomUser.getId())
                .team(roomUser.getTeam())
                .build();
    }
}
