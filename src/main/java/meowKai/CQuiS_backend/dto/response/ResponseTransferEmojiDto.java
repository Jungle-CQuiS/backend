package meowKai.CQuiS_backend.dto.response;

import lombok.*;
import meowKai.CQuiS_backend.domain.EmojiType;

@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class ResponseTransferEmojiDto {

    private EmojiType emojiType;
    private Long RoomUserId;
}
