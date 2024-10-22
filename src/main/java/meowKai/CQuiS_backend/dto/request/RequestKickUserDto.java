package meowKai.CQuiS_backend.dto.request;

import lombok.*;

@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class RequestKickUserDto {

    private Long roomUserId;
    private Long kickRoomUserId;
    private Long roomId;
}