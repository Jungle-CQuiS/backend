package meowKai.CQuiS_backend.dto.request;

import lombok.*;

import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class RequestJoinRoomDto {

    private UUID uuid;
    private Long roomId;
    private Long password;
}
