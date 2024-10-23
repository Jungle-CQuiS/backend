package meowKai.CQuiS_backend.dto.response;

import lombok.*;

@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class ResponseKickUserDto {

    private Long kickedRoomUserId;
}
