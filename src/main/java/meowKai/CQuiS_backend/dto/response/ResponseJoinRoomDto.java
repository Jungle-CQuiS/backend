package meowKai.CQuiS_backend.dto.response;

import lombok.*;
import meowKai.CQuiS_backend.domain.RoomUserRole;
import meowKai.CQuiS_backend.domain.RoomUserTeam;

@Getter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class ResponseJoinRoomDto {

    private Long roomUserId;
    private String sessionId; // openVidu
    private String token; // openVidu
}