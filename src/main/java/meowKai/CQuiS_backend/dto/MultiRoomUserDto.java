package meowKai.CQuiS_backend.dto;

import lombok.*;
import meowKai.CQuiS_backend.domain.RoomUserRole;
import meowKai.CQuiS_backend.domain.RoomUserTeam;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class MultiRoomUserDto {

    private Long roomUserId;
    private String username;
    private Integer honorCount;
    private RoomUserRole role;
    private RoomUserTeam team;
    private Boolean isLeader;
    private Boolean isReady;
}
