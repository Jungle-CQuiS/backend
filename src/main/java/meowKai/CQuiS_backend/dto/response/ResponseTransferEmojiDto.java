package meowKai.CQuiS_backend.dto.response;

import lombok.*;
import meowKai.CQuiS_backend.domain.EmojiType;
import meowKai.CQuiS_backend.domain.ResponseStatus;

@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class ResponseTransferEmojiDto {

    private ResponseStatus responseStatus;
    private EmojiType emojiType;
    private Long roomUserId;
}
