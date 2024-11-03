package meowKai.CQuiS_backend.dto.response;

import lombok.*;
import meowKai.CQuiS_backend.domain.RoomUserRole;
import meowKai.CQuiS_backend.domain.RoomUserTeam;

@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class ResponseGetUserInfoDto {
    private String username;
    private RoomUserRole role;
    private RoomUserTeam team;
    private Boolean isLeader;
}
