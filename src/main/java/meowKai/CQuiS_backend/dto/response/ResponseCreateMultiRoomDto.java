package meowKai.CQuiS_backend.dto.response;

import lombok.*;
import meowKai.CQuiS_backend.domain.RoomUserRole;
import meowKai.CQuiS_backend.domain.RoomUserTeam;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class ResponseCreateMultiRoomDto {

    private Long roomId;
    private RoomUserRole role;
    private Boolean isLeader;
    private RoomUserTeam team;
}
