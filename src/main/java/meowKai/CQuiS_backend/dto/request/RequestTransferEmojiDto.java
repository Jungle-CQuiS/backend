package meowKai.CQuiS_backend.dto.request;

import lombok.*;
import meowKai.CQuiS_backend.domain.EmojiType;
import meowKai.CQuiS_backend.domain.RoomUserTeam;

@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class RequestTransferEmojiDto {

    private RoomUserTeam teamColor;
    private EmojiType emojiType;
    private Long roomUserId;
    private Long roomId;
}
